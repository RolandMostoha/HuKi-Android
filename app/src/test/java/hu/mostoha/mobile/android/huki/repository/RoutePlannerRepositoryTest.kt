package hu.mostoha.mobile.android.huki.repository

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.interactor.exception.RoutePlannerLimitReachedException
import hu.mostoha.mobile.android.huki.model.domain.GraphhopperLimitState
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlanType
import hu.mostoha.mobile.android.huki.model.domain.RoutingMode
import hu.mostoha.mobile.android.huki.model.mapper.RoutePlannerNetworkModelMapper
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Profile
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import hu.mostoha.mobile.android.huki.network.GraphhopperService
import hu.mostoha.mobile.android.huki.network.HukiRoutingService
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.Instant

class RoutePlannerRepositoryTest {

    private val graphhopperService = mockk<GraphhopperService>()
    private val hukiRoutingService = mockk<HukiRoutingService>()
    private val graphhopperLimitRepository = mockk<GraphhopperLimitRepository>(relaxUnitFun = true)
    private val appConfiguration = mockk<AppConfiguration>()
    private val dateTimeProvider = mockk<DateTimeProvider>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)

    private val repository = RoutePlannerRepository(
        graphhopperService = graphhopperService,
        hukiRoutingService = hukiRoutingService,
        graphhopperLimitRepository = graphhopperLimitRepository,
        routePlannerNetworkModelMapper = RoutePlannerNetworkModelMapper(),
        gpxConfiguration = mockk(),
        appConfiguration = appConfiguration,
        dateTimeProvider = dateTimeProvider,
        analyticsService = analyticsService,
    )

    @Before
    fun setUp() {
        every { appConfiguration.getRoutingMode() } returns RoutingMode.AUTO
        every { dateTimeProvider.nowInMillis() } returns NOW_MILLIS
        givenLimitState(isReserveReached = false, isBlocked = false)
    }

    @Test
    fun `Given no limit, when getRoutePlan, then GraphHopper is used`() = runTest {
        givenGraphhopperSuccess(remaining = 2000)

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify(exactly = 1) { graphhopperService.getRoute(any()) }
        coVerify(exactly = 0) { hukiRoutingService.getRoute(any()) }
        coVerify(exactly = 0) { graphhopperLimitRepository.setReserveReachedUntil(any()) }
    }

    @Test
    fun `Given remaining below reserve, when getRoutePlan, then reserve reached is persisted until reset`() = runTest {
        givenGraphhopperSuccess(remaining = 499)

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify { graphhopperLimitRepository.setReserveReachedUntil(NOW_MILLIS + RESET_SECONDS * 1000) }
        verify { analyticsService.routePlannerReserveReached() }
    }

    @Test
    fun `Given GraphHopper 429 in Hungary, when getRoutePlan, then blocked is persisted and HuKi-Routing serves`() =
        runTest {
            givenGraphhopperTooManyRequests()
            givenHukiRoutingSuccess()

            val routePlan = repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

            assertThat(routePlan).isNotNull()
            coVerify { graphhopperLimitRepository.setBlockedUntil(NOW_MILLIS + RESET_SECONDS * 1000) }
            coVerify { hukiRoutingService.getRoute(match { it.profile == Profile.HIKE_HUKI && it.customModel == null }) }
            verify { analyticsService.routePlannerGraphhopperLimitHit() }
            verify { analyticsService.routePlannerServedByHukiRouting() }
        }

    @Test
    fun `Given GraphHopper 429 without reset header, when getRoutePlan, then blocked until next UTC midnight`() =
        runTest {
            givenGraphhopperTooManyRequests(resetSeconds = null)
            givenHukiRoutingSuccess()

            repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

            coVerify { graphhopperLimitRepository.setBlockedUntil(NEXT_UTC_MIDNIGHT_MILLIS) }
        }

    @Test
    fun `Given GraphHopper 429 with credits left, when getRoutePlan, then blocked is not persisted`() = runTest {
        givenGraphhopperTooManyRequests(remaining = 1200)
        givenHukiRoutingSuccess()

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify(exactly = 0) { graphhopperLimitRepository.setBlockedUntil(any()) }
        coVerify(exactly = 1) { hukiRoutingService.getRoute(any()) }
    }

    @Test
    fun `Given blocked round trip with stale waypoint abroad, when getRoutePlan, then HuKi-Routing serves`() =
        runTest {
            givenLimitState(isReserveReached = false, isBlocked = true)
            givenHukiRoutingSuccess()

            repository.getRoutePlan(RoutePlanType.RoundTrip(10_000), ABROAD_WAYPOINTS)

            coVerify(exactly = 1) { hukiRoutingService.getRoute(any()) }
        }

    @Test
    fun `Given GraphHopper 429 abroad, when getRoutePlan, then limit reached is thrown`() = runTest {
        givenGraphhopperTooManyRequests()

        assertLimitReached(ABROAD_WAYPOINTS)
        coVerify(exactly = 0) { hukiRoutingService.getRoute(any()) }
    }

    @Test
    fun `Given GraphHopper 429 and HuKi-Routing failure, when getRoutePlan, then limit reached is thrown`() = runTest {
        givenGraphhopperTooManyRequests()
        givenHukiRoutingFailure()

        assertLimitReached(HUNGARY_WAYPOINTS)
        verify { analyticsService.routePlannerHukiRoutingFailed() }
    }

    @Test
    fun `Given blocked in Hungary, when getRoutePlan, then HuKi-Routing serves without GraphHopper`() = runTest {
        givenLimitState(isReserveReached = true, isBlocked = true)
        givenHukiRoutingSuccess()

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify(exactly = 1) { hukiRoutingService.getRoute(any()) }
        coVerify(exactly = 0) { graphhopperService.getRoute(any()) }
    }

    @Test
    fun `Given blocked abroad, when getRoutePlan, then limit reached is thrown without any request`() = runTest {
        givenLimitState(isReserveReached = true, isBlocked = true)

        assertLimitReached(ABROAD_WAYPOINTS)
        coVerify(exactly = 0) { hukiRoutingService.getRoute(any()) }
        coVerify(exactly = 0) { graphhopperService.getRoute(any()) }
    }

    @Test
    fun `Given blocked and HuKi-Routing failure, when getRoutePlan, then limit reached is thrown`() = runTest {
        givenLimitState(isReserveReached = true, isBlocked = true)
        givenHukiRoutingFailure()

        assertLimitReached(HUNGARY_WAYPOINTS)
        coVerify(exactly = 0) { graphhopperService.getRoute(any()) }
    }

    @Test
    fun `Given reserve reached in Hungary, when getRoutePlan, then HuKi-Routing serves without GraphHopper`() = runTest {
        givenLimitState(isReserveReached = true, isBlocked = false)
        givenHukiRoutingSuccess()

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify(exactly = 1) { hukiRoutingService.getRoute(any()) }
        coVerify(exactly = 0) { graphhopperService.getRoute(any()) }
    }

    @Test
    fun `Given reserve reached and HuKi-Routing failure, when getRoutePlan, then GraphHopper serves`() = runTest {
        givenLimitState(isReserveReached = true, isBlocked = false)
        givenHukiRoutingFailure()
        givenGraphhopperSuccess(remaining = 400)

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify(exactly = 1) { graphhopperService.getRoute(any()) }
        verify { analyticsService.routePlannerGraphhopperAfterHukiRoutingFailure() }
        verify(exactly = 0) { analyticsService.routePlannerReserveReached() }
    }

    @Test
    fun `Given reserve reached, HuKi-Routing failure and GraphHopper 429, when getRoutePlan, then limit reached`() =
        runTest {
            givenLimitState(isReserveReached = true, isBlocked = false)
            givenHukiRoutingFailure()
            givenGraphhopperTooManyRequests()

            assertLimitReached(HUNGARY_WAYPOINTS)
            coVerify(exactly = 1) { hukiRoutingService.getRoute(any()) }
        }

    @Test
    fun `Given reserve reached abroad, when getRoutePlan, then GraphHopper serves`() = runTest {
        givenLimitState(isReserveReached = true, isBlocked = false)
        givenGraphhopperSuccess(remaining = 400)

        repository.getRoutePlan(RoutePlanType.Hike, ABROAD_WAYPOINTS)

        coVerify(exactly = 1) { graphhopperService.getRoute(any()) }
        coVerify(exactly = 0) { hukiRoutingService.getRoute(any()) }
    }

    @Test
    fun `Given GraphHopper only mode, when getRoutePlan, then limit state is ignored`() = runTest {
        every { appConfiguration.getRoutingMode() } returns RoutingMode.GRAPHHOPPER_ONLY
        givenLimitState(isReserveReached = true, isBlocked = true)
        givenGraphhopperSuccess(remaining = 2000)

        repository.getRoutePlan(RoutePlanType.Hike, HUNGARY_WAYPOINTS)

        coVerify(exactly = 1) { graphhopperService.getRoute(any()) }
        coVerify(exactly = 0) { hukiRoutingService.getRoute(any()) }
    }

    @Test
    fun `Given HuKi-Routing only mode, when getRoutePlan, then only HuKi-Routing is used`() = runTest {
        every { appConfiguration.getRoutingMode() } returns RoutingMode.HUKI_ROUTING_ONLY
        givenHukiRoutingSuccess()

        repository.getRoutePlan(RoutePlanType.Hike, ABROAD_WAYPOINTS)

        coVerify(exactly = 1) { hukiRoutingService.getRoute(any()) }
        coVerify(exactly = 0) { graphhopperService.getRoute(any()) }
    }

    private suspend fun assertLimitReached(waypoints: List<Location>) {
        val result = runCatching { repository.getRoutePlan(RoutePlanType.Hike, waypoints) }

        assertThat(result.exceptionOrNull()).isInstanceOf(RoutePlannerLimitReachedException::class.java)
    }

    private fun givenLimitState(isReserveReached: Boolean, isBlocked: Boolean) {
        coEvery { graphhopperLimitRepository.getLimitState(any()) } returns GraphhopperLimitState(
            isReserveReached = isReserveReached,
            isBlocked = isBlocked,
        )
    }

    private fun givenGraphhopperSuccess(remaining: Int) {
        coEvery { graphhopperService.getRoute(any()) } returns Response.success(
            routeResponse(),
            headersOf(
                "X-RateLimit-Remaining", remaining.toString(),
                "X-RateLimit-Reset", RESET_SECONDS.toString(),
            )
        )
    }

    private fun givenGraphhopperTooManyRequests(resetSeconds: Long? = RESET_SECONDS, remaining: Int = 0) {
        val headers = listOfNotNull(
            "X-RateLimit-Remaining" to remaining.toString(),
            resetSeconds?.let { "X-RateLimit-Reset" to it.toString() },
        )
        val rawResponse = okhttp3.Response.Builder()
            .code(429)
            .message("Too Many Requests")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://graphhopper.com/api/1/route").build())
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .build()

        coEvery { graphhopperService.getRoute(any()) } returns Response.error(
            "{}".toResponseBody("application/json".toMediaType()),
            rawResponse
        )
    }

    private fun givenHukiRoutingSuccess() {
        coEvery { hukiRoutingService.getRoute(any(), any()) } returns routeResponse()
    }

    private fun givenHukiRoutingFailure() {
        coEvery { hukiRoutingService.getRoute(any(), any()) } throws HttpException(
            Response.error<RouteResponse>(400, "{}".toResponseBody("application/json".toMediaType()))
        )
    }

    private fun routeResponse(): RouteResponse {
        val json = javaClass.classLoader!!.getResource("huki_routing_route_response.json").readText()

        return Moshi.Builder().build().adapter(RouteResponse::class.java).fromJson(json)!!
    }

    companion object {
        private val NOW_MILLIS = Instant.parse("2026-10-10T14:00:00Z").toEpochMilli()
        private val NEXT_UTC_MIDNIGHT_MILLIS = Instant.parse("2026-10-11T00:00:00Z").toEpochMilli()
        private const val RESET_SECONDS = 36_000L

        private val HUNGARY_WAYPOINTS = listOf(Location(47.4979, 19.0402), Location(47.52, 19.0))
        private val ABROAD_WAYPOINTS = listOf(Location(47.4979, 19.0402), Location(47.0707, 15.4395))
    }

}

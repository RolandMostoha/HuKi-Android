package hu.mostoha.mobile.android.huki.repository

import android.net.Uri
import com.codebutchery.androidgpx.data.GPXDocument
import com.codebutchery.androidgpx.data.GPXSegment
import com.codebutchery.androidgpx.data.GPXTrack
import com.codebutchery.androidgpx.data.GPXTrackPoint
import com.codebutchery.androidgpx.data.GPXWayPoint
import com.codebutchery.androidgpx.print.GPXFilePrinter
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.RoutePlannerLimitReachedException
import hu.mostoha.mobile.android.huki.interactor.isTooManyRequests
import hu.mostoha.mobile.android.huki.model.domain.GraphhopperLimitState
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.RoutePlanType
import hu.mostoha.mobile.android.huki.model.domain.RoutingMode
import hu.mostoha.mobile.android.huki.model.mapper.RoutePlannerNetworkModelMapper
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.network.GraphhopperService
import hu.mostoha.mobile.android.huki.network.HukiRoutingService
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointItem
import hu.mostoha.mobile.android.huki.util.GRAPHHOPPER_RESERVE_CREDITS
import hu.mostoha.mobile.android.huki.util.HUKI_ROUTING_BOUNDING_BOX
import hu.mostoha.mobile.android.huki.util.contains
import kotlinx.coroutines.CancellationException
import okhttp3.Headers
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("LongParameterList")
class RoutePlannerRepository @Inject constructor(
    private val graphhopperService: GraphhopperService,
    private val hukiRoutingService: HukiRoutingService,
    private val graphhopperLimitRepository: GraphhopperLimitRepository,
    private val routePlannerNetworkModelMapper: RoutePlannerNetworkModelMapper,
    private val gpxConfiguration: GpxConfiguration,
    private val appConfiguration: AppConfiguration,
    private val dateTimeProvider: DateTimeProvider,
    private val analyticsService: AnalyticsService,
) {

    suspend fun getRoutePlan(planType: RoutePlanType, waypoints: List<Location>): RoutePlan {
        val routeResponse = when (appConfiguration.getRoutingMode()) {
            RoutingMode.AUTO -> {
                val limitState = graphhopperLimitRepository.getLimitState(dateTimeProvider.nowInMillis())
                getRouteWithFallback(planType, waypoints, limitState)
            }
            RoutingMode.GRAPHHOPPER_ONLY -> getGraphhopperRoute(planType, waypoints)
            RoutingMode.HUKI_ROUTING_ONLY -> {
                val routeRequest = routePlannerNetworkModelMapper.createHukiRoutingRouteRequest(planType, waypoints)
                hukiRoutingService.getRoute(routeRequest)
            }
        }

        return routePlannerNetworkModelMapper.mapRouteResponse(planType, routeResponse)
    }

    private suspend fun getRouteWithFallback(
        planType: RoutePlanType,
        waypoints: List<Location>,
        limitState: GraphhopperLimitState,
    ): RouteResponse {
        val routedWaypoints = if (planType is RoutePlanType.RoundTrip) waypoints.take(1) else waypoints
        val isInHukiRoutingArea = routedWaypoints.all { HUKI_ROUTING_BOUNDING_BOX.contains(it) }

        if (limitState.isBlocked) {
            val hukiRoutingResponse = if (isInHukiRoutingArea) getHukiRoutingRoute(planType, waypoints) else null

            return hukiRoutingResponse ?: throw RoutePlannerLimitReachedException()
        }

        if (limitState.isReserveReached && isInHukiRoutingArea) {
            getHukiRoutingRoute(planType, waypoints)?.let { return it }

            analyticsService.routePlannerGraphhopperAfterHukiRoutingFailure()

            return getGraphhopperRoute(planType, waypoints)
        }

        return try {
            getGraphhopperRoute(planType, waypoints)
        } catch (exception: RoutePlannerLimitReachedException) {
            val hukiRoutingResponse = if (isInHukiRoutingArea) getHukiRoutingRoute(planType, waypoints) else null

            hukiRoutingResponse ?: throw exception
        }
    }

    private suspend fun getGraphhopperRoute(planType: RoutePlanType, waypoints: List<Location>): RouteResponse {
        val routeRequest = routePlannerNetworkModelMapper.createRouteRequest(planType, waypoints)
        val response = graphhopperService.getRoute(routeRequest)
        val routeResponse = response.body()

        if (response.isSuccessful && routeResponse != null) {
            updateReserveReached(response.headers())

            return routeResponse
        }

        val httpException = HttpException(response)
        if (httpException.isTooManyRequests()) {
            // A 429 with daily credits left is a short burst limit, so only this request falls back
            val remaining = response.headers()[HEADER_RATE_LIMIT_REMAINING]?.toIntOrNull()
            if (remaining == null || remaining <= 0) {
                graphhopperLimitRepository.setBlockedUntil(response.headers().rateLimitResetMillis())
            }
            analyticsService.routePlannerGraphhopperLimitHit()

            throw RoutePlannerLimitReachedException(httpException)
        }

        throw httpException
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun getHukiRoutingRoute(planType: RoutePlanType, waypoints: List<Location>): RouteResponse? {
        val routeRequest = routePlannerNetworkModelMapper.createHukiRoutingRouteRequest(planType, waypoints)

        return try {
            hukiRoutingService.getRoute(routeRequest).also {
                analyticsService.routePlannerServedByHukiRouting()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Timber.w(exception, "HuKi-Routing failed")
            analyticsService.routePlannerHukiRoutingFailed()

            null
        }
    }

    private suspend fun updateReserveReached(headers: Headers) {
        val remaining = headers[HEADER_RATE_LIMIT_REMAINING]?.toIntOrNull() ?: return
        if (remaining >= GRAPHHOPPER_RESERVE_CREDITS) return

        val nowMillis = dateTimeProvider.nowInMillis()
        if (graphhopperLimitRepository.getLimitState(nowMillis).isReserveReached) return

        graphhopperLimitRepository.setReserveReachedUntil(headers.rateLimitResetMillis())
        analyticsService.routePlannerReserveReached()
    }

    private fun Headers.rateLimitResetMillis(): Long {
        val nowMillis = dateTimeProvider.nowInMillis()
        val resetSeconds = this[HEADER_RATE_LIMIT_RESET]?.toLongOrNull()

        return if (resetSeconds != null) {
            nowMillis + TimeUnit.SECONDS.toMillis(resetSeconds)
        } else {
            Instant.ofEpochMilli(nowMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }
    }

    suspend fun saveRoutePlan(routePlan: RoutePlanUiModel, waypoints: List<WaypointItem>): Uri? {
        val routePlannerFilesDirPath = gpxConfiguration.getRoutePlannerGpxDirectory()
        val geoPoints = routePlan.geoPoints

        val gpxTrack = GPXTrack()
        val gpxSegment = GPXSegment()

        geoPoints.forEach { geoPoint ->
            val gpxTrackPoint = GPXTrackPoint(geoPoint.latitude.toFloat(), geoPoint.longitude.toFloat())
            gpxTrackPoint.elevation = geoPoint.altitude.toFloat()

            gpxSegment.addPoint(gpxTrackPoint)
        }
        gpxTrack.name = routePlan.name
        gpxTrack.addSegment(gpxSegment)

        val gpxTracks = listOf(gpxTrack)

        val gpxWaypoints = waypoints.mapNotNull {
            val location = it.location ?: return@mapNotNull null
            val gpxWayPoint = GPXWayPoint(location.latitude.toFloat(), location.longitude.toFloat())

            gpxWayPoint.name = it.waypointComment?.name
            gpxWayPoint.description = it.waypointComment?.comment

            gpxWayPoint
        }

        val gpxDocument = GPXDocument(gpxWaypoints, gpxTracks, emptyList())
        val filePath = "$routePlannerFilesDirPath/${routePlan.name}.gpx"

        val fileName = saveGpxFile(filePath, gpxDocument)
        val file = File(fileName)

        return Uri.fromFile(file)
    }

    private suspend fun saveGpxFile(filePath: String, gpxDocument: GPXDocument) = suspendCoroutine {
        val listener = object : GPXFilePrinter.GPXFilePrinterListener {
            override fun onGPXPrintStarted() = Unit

            override fun onGPXPrintCompleted() {
                it.resume(filePath)
            }

            override fun onGPXPrintError(message: String?) {
                Timber.w(message)

                it.resumeWithException(DomainException(Message.Res(R.string.route_planner_general_error_message)))
            }
        }

        val printer = GPXFilePrinter(listener)

        printer.print(gpxDocument, filePath)
    }

    companion object {
        private const val HEADER_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining"
        private const val HEADER_RATE_LIMIT_RESET = "X-RateLimit-Reset"
    }

}

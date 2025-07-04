package hu.mostoha.mobile.android.huki.ui.home.routeplanner

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.toMillis
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.RoutePlanType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.mapper.RoutePlannerUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.WaypointComment
import hu.mostoha.mobile.android.huki.model.ui.WaypointCommentResult
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.util.DEFAULT_LOCAL_DATE
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.answerDefaults
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.osmdroid.util.GeoPoint
import kotlin.time.Duration.Companion.minutes

@ExperimentalCoroutinesApi
class RoutePlannerViewModelTest {

    private lateinit var viewModel: RoutePlannerViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val routePlannerRepository = mockk<RoutePlannerRepository>()
    private val placeHistoryRepository = mockk<PlaceHistoryRepository>()
    private val geocodingRepository = mockk<GeocodingRepository>()
    private val routePlannerUiModelMapper = RoutePlannerUiModelMapper()
    private val myLocationProvider = mockk<AsyncMyLocationProvider>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)
    private val dateTimeProvider = mockk<DateTimeProvider>()
    private val gpxFileUri = mockk<Uri>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        coEvery { placeHistoryRepository.savePlace(any(), any()) } returns Unit

        dateTimeProvider.answerDefaults()

        viewModel = RoutePlannerViewModel(
            exceptionLogger,
            analyticsService,
            myLocationProvider,
            routePlannerRepository,
            placeHistoryRepository,
            geocodingRepository,
            routePlannerUiModelMapper,
            dateTimeProvider
        )
    }

    @Test
    fun `When initWaypoints, then empty waypoints are emitted`() {
        runTestDefault {
            viewModel.waypointItems.test {
                viewModel.initWaypoints()

                assertThat(awaitItem()).isEmpty()
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            waypointType = WaypointType.START
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[1].id,
                            order = 1,
                            waypointType = WaypointType.END
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given place, when initWaypoint, then new waypoint is emitted with place details`() {
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL_1

            viewModel.waypointItems.test {
                viewModel.initWaypoint(placeUiModel)

                assertThat(awaitItem()).isEmpty()
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            waypointType = WaypointType.START
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[1].id,
                            order = 1,
                            waypointType = WaypointType.END,
                            primaryText = placeUiModel.primaryText,
                            location = placeUiModel.geoPoint.toLocation(),
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given locations, when initWaypoints, then waypoint items are emitted`() {
        runTestDefault {
            val locations = listOf(
                DEFAULT_PLACE_UI_MODEL_1.primaryText to DEFAULT_PLACE_UI_MODEL_1.geoPoint,
                DEFAULT_PLACE_UI_MODEL_2.primaryText to DEFAULT_PLACE_UI_MODEL_2.geoPoint,
            )

            viewModel.waypointItems.test {
                viewModel.initWaypoints(locations)

                assertThat(awaitItem()).isEmpty()
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            primaryText = DEFAULT_PLACE_UI_MODEL_1.primaryText,
                            location = DEFAULT_PLACE_UI_MODEL_1.geoPoint.toLocation(),
                            waypointType = WaypointType.START,
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[1].id,
                            order = 1,
                            primaryText = DEFAULT_PLACE_UI_MODEL_2.primaryText,
                            location = DEFAULT_PLACE_UI_MODEL_2.geoPoint.toLocation(),
                            waypointType = WaypointType.END,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given waypoint, when updateWaypoint, then waypoint is updated with the details`() {
        runTestDefault {
            val searchText = "Dob"
            val placeName = DEFAULT_PLACE_UI_MODEL_1.primaryText
            val location = DEFAULT_PLACE_UI_MODEL_1.geoPoint.toLocation()

            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)

                viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText)

                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            waypointType = WaypointType.START,
                            primaryText = placeName,
                            location = location,
                            searchText = searchText,
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[1].id,
                            order = 1,
                            waypointType = WaypointType.END,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given waypoint, when updateWaypoint, then place is saved in history`() {
        runTestDefault {
            val searchText = "Dob"

            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)
                viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText)
                skipItems(1)
            }

            coVerify { placeHistoryRepository.savePlace(DEFAULT_PLACE_UI_MODEL_1, DEFAULT_LOCAL_DATE.toMillis()) }
        }
    }

    @Test
    fun `When addEmptyWaypoint, then waypoints are emitted with new empty waypoint`() {
        runTestDefault {
            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)

                viewModel.addEmptyWaypoint()

                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            waypointType = WaypointType.START
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[1].id,
                            order = 1,
                            waypointType = WaypointType.INTERMEDIATE
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[2].id,
                            order = 2,
                            waypointType = WaypointType.END
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given waypoint, when removeWaypoint, then waypoint is removed`() {
        runTestDefault {
            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)

                viewModel.removeWaypoint(viewModel.waypointItems.value[0])

                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            waypointType = WaypointType.START
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given from and to waypoints, when swapWaypoints, then waypoints are swapped`() {
        runTestDefault {
            val searchText = "Dob"
            val placeName = DEFAULT_PLACE_UI_MODEL_1.primaryText
            val location = DEFAULT_PLACE_UI_MODEL_1.geoPoint.toLocation()

            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)
                viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_1, searchText)
                skipItems(1)

                viewModel.swapWaypoints(viewModel.waypointItems.value.reversed())

                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        WaypointItem(
                            id = viewModel.waypointItems.value[0].id,
                            order = 0,
                            waypointType = WaypointType.START,
                            primaryText = placeName,
                            location = location,
                            searchText = searchText,
                        ),
                        WaypointItem(
                            id = viewModel.waypointItems.value[1].id,
                            order = 1,
                            waypointType = WaypointType.END,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given two not empty waypoints, when updateWaypoints, then route plan is updated`() {
        runTestDefault {
            val searchText1 = "Dob"
            val searchText2 = "Szánkó"
            coEvery { routePlannerRepository.getRoutePlan(any(), any()) } returns DEFAULT_ROUTE_PLAN
            coEvery { routePlannerRepository.saveRoutePlan(any(), any()) } returns null

            viewModel.routePlanUiModel.test {
                viewModel.initWaypoints()
                advanceUntilIdle()
                viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText1)
                viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_2, searchText2)
                advanceUntilIdle()

                assertThat(awaitItem()).isNull()
                val actualRoutePlanUiModel = awaitItem()!!
                val waypointItems = viewModel.waypointItems.value
                assertThat(actualRoutePlanUiModel.triggerLocations).isEqualTo(waypointItems.map { it.location })
                assertThat(actualRoutePlanUiModel.geoPoints).isEqualTo(DEFAULT_ROUTE_PLAN.wayPoints.toGeoPoints())
            }
        }
    }

    @Test
    fun `Given waypoint result, when addWaypointComment, then waypoint items are updated`() {
        runTestDefault {
            val waypointComment = WaypointComment(
                name = "Comment name",
                comment = "Comment"
            )

            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)

                val waypointItems = viewModel.waypointItems.value
                val commentResult = WaypointCommentResult(waypointItems[0].id, waypointComment)

                viewModel.addWaypointComment(commentResult)

                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        waypointItems[0].copy(
                            waypointComment = waypointComment
                        ),
                        waypointItems[1]
                    )
                )
            }
        }
    }

    @Test
    fun `Given two not empty waypoints, when returnToHome, then route plan is updated`() {
        runTestDefault {
            val searchText1 = "Dob"
            val searchText2 = "Szánkó"
            coEvery { routePlannerRepository.getRoutePlan(any(), any()) } returns DEFAULT_ROUTE_PLAN
            coEvery { routePlannerRepository.saveRoutePlan(any(), any()) } returns null

            viewModel.routePlanUiModel.test {
                viewModel.initWaypoints()
                advanceUntilIdle()
                viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText1)
                viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_2, searchText2)
                advanceUntilIdle()
                viewModel.returnToHome()
                advanceUntilIdle()

                skipItems(2)

                val triggerLocations = awaitItem()!!.triggerLocations
                assertThat(triggerLocations.first()).isEqualTo(triggerLocations.last())
            }
        }
    }

    @Test
    fun `Given plan type, when switchPlanType, then route plan is updated`() {
        runTestDefault {
            val searchText1 = "Dob"
            val searchText2 = "Szánkó"
            coEvery { routePlannerRepository.getRoutePlan(any(), any()) } returns DEFAULT_ROUTE_PLAN
            coEvery { routePlannerRepository.saveRoutePlan(any(), any()) } returns null

            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                advanceUntilIdle()
                viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText1)
                viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_2, searchText2)
                advanceUntilIdle()
                viewModel.switchPlanType(RoutePlanType.RoundTrip())

                skipItems(3)

                assertThat(awaitItem().size).isEqualTo(1)
            }
        }
    }

    @Test
    fun `Given GPX file URI, when saveRoutePlan, then file URI is emitted`() {
        runTestDefault {
            val searchText1 = "Dob"
            val searchText2 = "Szánkó"
            coEvery { routePlannerRepository.getRoutePlan(any(), any()) } returns DEFAULT_ROUTE_PLAN
            coEvery { routePlannerRepository.saveRoutePlan(any(), any()) } returns gpxFileUri

            viewModel.initWaypoints()
            advanceUntilIdle()
            viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText1)
            viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_2, searchText2)
            advanceUntilIdle()

            viewModel.routePlanGpxFileUri.test {
                viewModel.saveRoutePlan()

                assertThat(awaitItem()).isEqualTo(gpxFileUri)
            }
        }
    }

    @Test
    fun `Given null URI, when saveRoutePlan, then error is emitted`() {
        runTestDefault {
            val searchText1 = "Dob"
            val searchText2 = "Szánkó"
            coEvery { routePlannerRepository.getRoutePlan(any(), any()) } returns DEFAULT_ROUTE_PLAN
            coEvery { routePlannerRepository.saveRoutePlan(any(), any()) } returns null

            viewModel.initWaypoints()
            advanceUntilIdle()
            viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText1)
            viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_2, searchText2)
            advanceUntilIdle()

            viewModel.routePlanErrorMessage.test {
                viewModel.saveRoutePlan()

                assertThat(awaitItem()).isEqualTo(R.string.route_planner_error_file_save_unsuccsessful.toMessage())
            }
        }
    }

    @Test
    fun `When clearRoutePlanner, then empty waypoints are emitted`() {
        runTestDefault {
            val searchText1 = "Dob"
            val searchText2 = "Szánkó"
            coEvery { routePlannerRepository.getRoutePlan(any(), any()) } returns DEFAULT_ROUTE_PLAN
            coEvery { routePlannerRepository.saveRoutePlan(any(), any()) } returns null

            viewModel.waypointItems.test {
                viewModel.initWaypoints()
                skipItems(2)
                viewModel.updateWaypoint(viewModel.waypointItems.value[0], DEFAULT_PLACE_UI_MODEL_1, searchText1)
                viewModel.updateWaypoint(viewModel.waypointItems.value[1], DEFAULT_PLACE_UI_MODEL_2, searchText2)
                skipItems(1)

                viewModel.clearRoutePlanner()

                assertThat(awaitItem()).isEqualTo(emptyList<WaypointItem>())
            }
        }
    }

    companion object {
        private val DEFAULT_PLACE_UI_MODEL_1 = PlaceUiModel(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            primaryText = DEFAULT_NODE_NAME.toMessage(),
            secondaryText = DEFAULT_NODE_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            boundingBox = null,
            placeFeature = PlaceFeature.ROUTE_PLANNER_SEARCH,
        )
        private val DEFAULT_PLACE_UI_MODEL_2 = PlaceUiModel(
            osmId = DEFAULT_WAY_OSM_ID,
            placeType = PlaceType.WAY,
            primaryText = DEFAULT_WAY_NAME.toMessage(),
            secondaryText = DEFAULT_WAY_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE),
            boundingBox = null,
            placeFeature = PlaceFeature.ROUTE_PLANNER_SEARCH,
        )
        private val DEFAULT_WAYPOINTS = listOf(
            Location(
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
            ),
            Location(
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
            )
        )
        private val DEFAULT_ROUTE_PLAN = RoutePlan(
            wayPoints = DEFAULT_WAYPOINTS,
            locations = DEFAULT_WAYPOINTS,
            travelTime = 100L.minutes,
            distance = 13000,
            altitudeRange = Pair(
                DEFAULT_WAYPOINTS.minOf { it.altitude!! }.toInt(),
                DEFAULT_WAYPOINTS.maxOf { it.altitude!! }.toInt()
            ),
            incline = 500,
            decline = 200,
            isClosed = false,
            planType = RoutePlanType.Hike
        )
    }

}

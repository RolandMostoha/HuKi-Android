package hu.mostoha.mobile.android.huki.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.mapper.HomeUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.OktRoutesMapper
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.MapUiModel
import hu.mostoha.mobile.android.huki.model.ui.MyLocationUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.OktRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_JEL
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_START_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_START_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.flowOfError
import hu.mostoha.mobile.android.huki.util.runTestDefault
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.osmdroid.util.GeoPoint

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val analyticsService = mockk<AnalyticsService>()
    private val placesRepository = mockk<PlacesRepository>()
    private val geocodingRepository = mockk<GeocodingRepository>()
    private val landscapeInteractor = mockk<LandscapeInteractor>()
    private val oktRepository = mockk<OktRepository>()
    private val myLocationProvider = mockk<AsyncMyLocationProvider>()
    private val homeUiModelMapper = mockk<HomeUiModelMapper>()
    private val oktRoutesMapper = OktRoutesMapper()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        every { exceptionLogger.recordException(any()) } returns Unit
        coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns null
        mockLandscapes()
        mockOktRoutes()

        viewModel = HomeViewModel(
            mainCoroutineRule.testDispatcher,
            exceptionLogger,
            analyticsService,
            placesRepository,
            geocodingRepository,
            oktRepository,
            landscapeInteractor,
            homeUiModelMapper,
            oktRoutesMapper,
        )
    }

    @Test
    fun `Given location, when loadLandscapes, then landscapes are emitted`() =
        runTestDefault {
            val landscapes = listOf(DEFAULT_LANDSCAPE)
            val landscapesUiModels = listOf(DEFAULT_LANDSCAPE_UI_MODEL)

            every { landscapeInteractor.requestGetLandscapesFlow(any()) } returns flowOf(landscapes)
            every { homeUiModelMapper.mapLandscapes(any()) } returns landscapesUiModels

            viewModel.landscapes.test {
                viewModel.loadLandscapes(DEFAULT_MY_LOCATION.toMockLocation())

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(landscapesUiModels)
            }
        }

    @Test
    fun `Given domain error, when init landscapes, then error message is emitted`() =
        runTestDefault {
            val domainException = UnknownException(Exception(""))
            every { landscapeInteractor.requestGetLandscapesFlow(any()) } returns flowOfError(domainException)

            viewModel.errorMessage.test {
                assertThat(awaitItem()).isEqualTo(R.string.error_message_unknown.toMessage())
            }
        }

    @Test
    fun `Given unexpected error, when init landscapes, then exception logger is called`() =
        runTestDefault {
            val unexpectedException = IllegalStateException(Exception(""))
            every { landscapeInteractor.requestGetLandscapesFlow(any()) } returns flowOfError(unexpectedException)

            advanceUntilIdle()

            verify {
                exceptionLogger.recordException(any())
            }
        }

    @Test
    fun `Given place, when loadPlace, then place details is posted`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            every { homeUiModelMapper.mapPlaceDetails(placeUiModel) } returns placeDetailsUiModel

            viewModel.placeDetails.test {
                viewModel.loadPlace(placeUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }

    @Test
    fun `Given place, when loadPlace, then follow location is disabled`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            every { homeUiModelMapper.mapPlaceDetails(placeUiModel) } returns placeDetailsUiModel

            viewModel.myLocationUiModel.test {
                viewModel.loadPlace(placeUiModel)

                assertThat(awaitItem()).isEqualTo(MyLocationUiModel())
                assertThat(awaitItem()).isEqualTo(MyLocationUiModel(isFollowLocationEnabled = false))
            }
        }

    @Test
    fun `Given place, when loadPlaceDetails, then place details is emitted`() =
        runTestDefault {
            val osmId = DEFAULT_PLACE.osmId
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
            coEvery { placesRepository.getGeometry(osmId, PlaceType.NODE) } returns geometry
            every { homeUiModelMapper.mapPlaceDetails(placeUiModel, geometry) } returns placeDetailsUiModel

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }

    @Test
    fun `When loadPlaceDetails, then follow location should be disabled`() =
        runTestDefault {
            val osmId = DEFAULT_PLACE.osmId
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
            coEvery { placesRepository.getGeometry(osmId, PlaceType.NODE) } returns geometry
            every { homeUiModelMapper.mapPlaceDetails(placeUiModel, geometry) } returns placeDetailsUiModel

            viewModel.myLocationUiModel.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isEqualTo(MyLocationUiModel())
                assertThat(awaitItem()).isEqualTo(MyLocationUiModel(isFollowLocationEnabled = false))
            }
        }

    @Test
    fun `Given domain error, when loadPlaceDetails, then error message is emitted`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery {
                placesRepository.getGeometry(placeUiModel.osmId, placeUiModel.placeType)
            } throws DomainException(errorRes)

            viewModel.errorMessage.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }

    @Test
    fun `Given place name and bounding box, when loadHikingRoutes, then hiking routes are emitted`() =
        runTestDefault {
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            val hikingRouteUiModels = listOf(HikingRoutesItem.Item(DEFAULT_HIKING_ROUTE_UI_MODEL))
            coEvery { placesRepository.getHikingRoutes(boundingBox) } returns hikingRoutes
            every { homeUiModelMapper.mapHikingRoutes(placeName, hikingRoutes) } returns hikingRouteUiModels

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(hikingRouteUiModels)
            }
        }

    @Test
    fun `Given domain error, when loadHikingRoutes, then error message is emitted`() =
        runTestDefault {
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery { placesRepository.getHikingRoutes(boundingBox) } throws DomainException(errorRes)

            viewModel.errorMessage.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }

    @Test
    fun `Given hiking route, when loadHikingRouteDetails, then hiking routes are emitted`() =
        runTestDefault {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
            val placeDetailsUiModel = DEFAULT_HIKING_ROUTE_DETAILS_UI_MODEL
            coEvery { placesRepository.getGeometry(osmId, any()) } returns geometry
            every {
                homeUiModelMapper.mapHikingRouteDetails(
                    hikingRouteUiModel,
                    geometry
                )
            } returns placeDetailsUiModel

            viewModel.placeDetails.test {
                viewModel.loadHikingRouteDetails(hikingRouteUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }

    @Test
    fun `When loadHikingRouteDetails, then follow location should be disabled`() =
        runTestDefault {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
            val placeDetailsUiModel = DEFAULT_HIKING_ROUTE_DETAILS_UI_MODEL
            coEvery { placesRepository.getGeometry(osmId, any()) } returns geometry
            every {
                homeUiModelMapper.mapHikingRouteDetails(
                    hikingRouteUiModel,
                    geometry
                )
            } returns placeDetailsUiModel

            viewModel.myLocationUiModel.test {
                viewModel.loadHikingRouteDetails(hikingRouteUiModel)

                assertThat(awaitItem()).isEqualTo(MyLocationUiModel())
                assertThat(awaitItem()).isEqualTo(MyLocationUiModel(isFollowLocationEnabled = false))
            }
        }

    @Test
    fun `When loadHikingRouteDetails, then hiking routes should be cleared`() =
        runTestDefault {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            val hikingRouteUiModels = listOf(HikingRoutesItem.Item(DEFAULT_HIKING_ROUTE_UI_MODEL))
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
            val placeDetailsUiModel = DEFAULT_HIKING_ROUTE_DETAILS_UI_MODEL
            coEvery { placesRepository.getGeometry(osmId, any()) } returns geometry
            every {
                homeUiModelMapper.mapHikingRouteDetails(
                    hikingRouteUiModel,
                    geometry
                )
            } returns placeDetailsUiModel
            coEvery { placesRepository.getHikingRoutes(any()) } returns hikingRoutes
            every { homeUiModelMapper.mapHikingRoutes(any(), any()) } returns hikingRouteUiModels

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(hikingRouteUiModels)

                viewModel.loadHikingRouteDetails(hikingRouteUiModel)
                assertThat(awaitItem()).isNull()
            }
        }

    @Test
    fun `Given domain error, when loadHikingRouteDetails, then error message is emitted`() =
        runTestDefault {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery { placesRepository.getGeometry(osmId, any()) } throws DomainException(errorRes)

            viewModel.errorMessage.test {
                viewModel.loadHikingRouteDetails(hikingRouteUiModel)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }

    @Test
    fun `Given OKT routes, when loadOktRoutes, then OKT routes are emitted`() =
        runTest {
            viewModel.oktRoutes.test {
                viewModel.loadOktRoutes()

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    oktRoutesMapper.map(DEFAULT_OKT_FULL_GEO_POINTS.toGeoPoints(), LOCAL_OKT_ROUTES)
                )
            }
        }

    @Test
    fun `Given OKT routes, when select OKT route by ID, then OKT routes are updated`() =
        runTest {
            viewModel.oktRoutes.test {
                viewModel.loadOktRoutes()

                advanceUntilIdle()

                viewModel.selectOktRoute("OKT-01")

                skipItems(2)

                assertThat(awaitItem()!!.routes[1].isSelected).isTrue()
            }
        }

    @Test
    fun `Given OKT routes, when select OKT route by geo point, then OKT routes are updated`() =
        runTest {
            viewModel.oktRoutes.test {
                viewModel.loadOktRoutes()

                advanceUntilIdle()

                viewModel.selectOktRoute(LOCAL_OKT_ROUTES[1].start.toGeoPoint())

                skipItems(2)

                assertThat(awaitItem()!!.routes[1].isSelected).isTrue()
            }
        }

    @Test
    fun `When updateMyLocationConfig, then my location UI model should be updated`() =
        runTestDefault {
            viewModel.myLocationUiModel.test {
                viewModel.updateMyLocationConfig(
                    isLocationPermissionEnabled = true,
                    isFollowLocationEnabled = false,
                )

                assertThat(awaitItem()).isEqualTo(MyLocationUiModel())
                assertThat(awaitItem()).isEqualTo(
                    MyLocationUiModel(
                        isLocationPermissionEnabled = true,
                        isFollowLocationEnabled = false,
                    )
                )
            }
        }

    @Test
    fun `When saveBoundingBox, then map UI model should be updated with the given bounding box with the default offset`() =
        runTestDefault {
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES

            viewModel.mapUiModel.test {
                viewModel.saveBoundingBox(boundingBox)

                assertThat(awaitItem()).isEqualTo(MapUiModel())
                assertThat(awaitItem()).isEqualTo(MapUiModel(boundingBox, withDefaultOffset = true))
            }
        }

    @Test
    fun `When clearPlaceDetails, then place details UI model should be cleared`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            val geometry = Geometry.Node(placeUiModel.osmId, Location(47.7193842, 18.8962014))
            coEvery { placesRepository.getGeometry(any(), any()) } returns geometry
            every { homeUiModelMapper.mapPlaceDetails(any(), any()) } returns placeDetailsUiModel

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetails(placeUiModel)
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)

                viewModel.clearPlaceDetails()
                assertThat(awaitItem()).isNull()
            }
        }

    @Test
    fun `When clearHikingRoutes, then hiking route UI models should be cleared`() =
        runTestDefault {
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            val hikingRouteUiModels = listOf(HikingRoutesItem.Item(DEFAULT_HIKING_ROUTE_UI_MODEL))
            coEvery { placesRepository.getHikingRoutes(boundingBox) } returns hikingRoutes
            every { homeUiModelMapper.mapHikingRoutes(placeName, hikingRoutes) } returns hikingRouteUiModels

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(hikingRouteUiModels)

                viewModel.clearHikingRoutes()
                assertThat(awaitItem()).isNull()
            }
        }

    private fun mockLandscapes() {
        val landscapes = listOf(DEFAULT_LANDSCAPE)
        val landscapeUiModels = listOf(DEFAULT_LANDSCAPE_UI_MODEL)

        every { landscapeInteractor.requestGetLandscapesFlow() } returns flowOf(landscapes)
        every { homeUiModelMapper.mapLandscapes(any()) } returns landscapeUiModels
    }

    private fun mockOktRoutes() {
        coEvery { oktRepository.getOktFullRoute() } returns DEFAULT_OKT_FULL_GEO_POINTS
    }

    companion object {
        private val DEFAULT_MY_LOCATION = Location(
            DEFAULT_MY_LOCATION_LATITUDE,
            DEFAULT_MY_LOCATION_LONGITUDE
        )
        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_PLACE.osmId,
            placeType = DEFAULT_PLACE.placeType,
            primaryText = DEFAULT_PLACE.name.toMessage(),
            secondaryText = null,
            iconRes = 0,
            geoPoint = DEFAULT_PLACE.location.toGeoPoint(),
            boundingBox = null,
        )
        private val DEFAULT_PLACE_DETAILS_UI_MODEL = PlaceDetailsUiModel(
            placeUiModel = DEFAULT_PLACE_UI_MODEL,
            geometryUiModel = GeometryUiModel.Node(DEFAULT_PLACE_UI_MODEL.geoPoint)
        )
        private val DEFAULT_HIKING_ROUTE = HikingRoute(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolType = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
        )
        private val DEFAULT_HIKING_ROUTE_UI_MODEL = HikingRouteUiModel(
            osmId = DEFAULT_HIKING_ROUTE.osmId,
            name = DEFAULT_HIKING_ROUTE.name,
            symbolIcon = DEFAULT_HIKING_ROUTE.symbolType.getIconRes()
        )
        private val DEFAULT_HIKING_ROUTE_GEOMETRY = Geometry.Relation(
            osmId = DEFAULT_HIKING_ROUTE_UI_MODEL.osmId,
            ways = listOf(
                Geometry.Way(
                    osmId = DEFAULT_HIKING_ROUTE_WAY_OSM_ID,
                    locations = DEFAULT_HIKING_ROUTE_WAY_GEOMETRY.map { Location(it.first, it.second) },
                    distance = 5000
                )
            )
        )
        private val DEFAULT_HIKING_ROUTE_DETAILS_UI_MODEL = PlaceDetailsUiModel(
            placeUiModel = PlaceUiModel(
                osmId = DEFAULT_HIKING_ROUTE.osmId,
                placeType = PlaceType.RELATION,
                primaryText = DEFAULT_HIKING_ROUTE.name.toMessage(),
                secondaryText = null,
                iconRes = 0,
                geoPoint = GeoPoint(DEFAULT_HIKING_ROUTE_START_LATITUDE, DEFAULT_HIKING_ROUTE_START_LONGITUDE),
                boundingBox = null,
            ),
            geometryUiModel = GeometryUiModel.Relation(
                ways = listOf(
                    GeometryUiModel.Way(
                        osmId = DEFAULT_HIKING_ROUTE_WAY_OSM_ID,
                        geoPoints = DEFAULT_HIKING_ROUTE_WAY_GEOMETRY.map { GeoPoint(it.first, it.second) },
                        isClosed = false
                    )
                )
            )
        )
        private val DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES = BoundingBox(
            north = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH,
            east = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST,
            south = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH,
            west = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
        )
        private val DEFAULT_LANDSCAPE = LOCAL_LANDSCAPES.first()
        private val DEFAULT_LANDSCAPE_UI_MODEL = LandscapeUiModel(
            osmId = DEFAULT_LANDSCAPE.osmId,
            osmType = DEFAULT_LANDSCAPE.osmType,
            name = DEFAULT_LANDSCAPE.nameRes.toMessage(),
            geoPoint = DEFAULT_LANDSCAPE.center.toGeoPoint(),
            iconRes = R.drawable.ic_landscapes_plain_land,
            markerRes = R.drawable.ic_marker_landscapes_plain_land,
        )
        private val DEFAULT_OKT_FULL_GEO_POINTS = listOf(
            LOCAL_OKT_ROUTES.first().start,
            LOCAL_OKT_ROUTES.first().end,
            LOCAL_OKT_ROUTES[1].start,
            LOCAL_OKT_ROUTES[1].end,
        )
    }

}

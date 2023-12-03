package hu.mostoha.mobile.android.huki.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.extensions.toMillis
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.mapper.HikingRouteRelationMapper
import hu.mostoha.mobile.android.huki.model.mapper.HomeUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.OktRoutesMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDomainUiMapper
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.MapUiModel
import hu.mostoha.mobile.android.huki.model.ui.MyLocationUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.OktRepository
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_JEL
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.util.DEFAULT_LOCAL_DATE
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.answerDefaults
import hu.mostoha.mobile.android.huki.util.flowOfError
import hu.mostoha.mobile.android.huki.util.runTestDefault
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val analyticsService = mockk<AnalyticsService>()
    private val placesRepository = mockk<PlacesRepository>()
    private val placeHistoryRepository = mockk<PlaceHistoryRepository>()
    private val geocodingRepository = mockk<GeocodingRepository>()
    private val landscapeInteractor = mockk<LandscapeInteractor>()
    private val oktRepository = mockk<OktRepository>()
    private val myLocationProvider = mockk<AsyncMyLocationProvider>()
    private val dateTimeProvider = mockk<DateTimeProvider>()
    private val placeDomainUiMapper = PlaceDomainUiMapper(HikingRouteRelationMapper())
    private val homeUiModelMapper = HomeUiModelMapper(placeDomainUiMapper)
    private val oktRoutesMapper = OktRoutesMapper()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        dateTimeProvider.answerDefaults()
        every { exceptionLogger.recordException(any()) } returns Unit
        coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns null
        coEvery { placeHistoryRepository.getPlaces() } returns flow { emptyList<Place>() }
        coEvery { placeHistoryRepository.savePlace(any(), any()) } returns Unit
        coEvery { placeHistoryRepository.clearOldPlaces() } returns Unit
        mockLandscapes()
        mockOktRoutes()

        viewModel = HomeViewModel(
            exceptionLogger,
            analyticsService,
            placesRepository,
            placeHistoryRepository,
            geocodingRepository,
            oktRepository,
            landscapeInteractor,
            homeUiModelMapper,
            placeDomainUiMapper,
            oktRoutesMapper,
            dateTimeProvider,
        )
    }

    @Test
    fun `Given location, when loadLandscapes, then landscapes are emitted`() =
        runTestDefault {
            val landscapes = listOf(DEFAULT_LANDSCAPE)

            every { landscapeInteractor.requestGetLandscapesFlow(any()) } returns flowOf(landscapes)

            viewModel.landscapes.test {
                viewModel.loadLandscapes(DEFAULT_MY_LOCATION.toMockLocation())

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(homeUiModelMapper.mapLandscapes(landscapes))
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
    fun `Given place, when loadPlaceDetails, then place details is emitted`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
            coVerify { placeHistoryRepository.savePlace(placeUiModel, DEFAULT_LOCAL_DATE.toMillis()) }
        }

    @Test
    fun `Given place, when loadPlaceDetails, then follow location is disabled`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL

            viewModel.myLocationUiModel.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isEqualTo(MyLocationUiModel())
                assertThat(awaitItem()).isEqualTo(MyLocationUiModel(isFollowLocationEnabled = false))
            }
        }

    @Test
    fun `Given place, when loadPlaceDetailsWithGeocoding, then place details is emitted`() =
        runTestDefault {
            val geoPoint = DEFAULT_PLACE_UI_MODEL.geoPoint
            val placeFeature = PlaceFeature.MAP_SEARCH

            coEvery { geocodingRepository.getPlace(any(), placeFeature) } returns null

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetailsWithGeocoding(geoPoint, placeFeature)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isNotNull()
            }
            coVerify { placeHistoryRepository.savePlace(any(), DEFAULT_LOCAL_DATE.toMillis()) }
        }

    @Test
    fun `Given place, when loadPlaceDetailsWithGeometry, then place details is emitted`() =
        runTestDefault {
            val osmId = DEFAULT_PLACE.osmId
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
            coEvery { placesRepository.getGeometry(osmId, PlaceType.NODE) } returns geometry

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetailsWithGeometry(placeUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }

    @Test
    fun `When loadPlaceDetailsWithGeometry, then follow location should be disabled`() =
        runTestDefault {
            val osmId = DEFAULT_PLACE.osmId
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
            coEvery { placesRepository.getGeometry(osmId, PlaceType.NODE) } returns geometry

            viewModel.myLocationUiModel.test {
                viewModel.loadPlaceDetailsWithGeometry(placeUiModel)

                assertThat(awaitItem()).isEqualTo(MyLocationUiModel())
                assertThat(awaitItem()).isEqualTo(MyLocationUiModel(isFollowLocationEnabled = false))
            }
        }

    @Test
    fun `Given domain error, when loadPlaceDetailsWithGeometry, then error message is emitted`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery {
                placesRepository.getGeometry(placeUiModel.osmId, placeUiModel.placeType)
            } throws DomainException(errorRes)

            viewModel.errorMessage.test {
                viewModel.loadPlaceDetailsWithGeometry(placeUiModel)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }

    @Test
    fun `Given place name and bounding box, when loadHikingRoutes, then hiking routes are emitted`() =
        runTestDefault {
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            coEvery { placesRepository.getHikingRoutes(boundingBox) } returns hikingRoutes

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(homeUiModelMapper.mapHikingRoutes(placeName, hikingRoutes))
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
            coEvery { placesRepository.getGeometry(osmId, any()) } returns geometry

            viewModel.placeDetails.test {
                viewModel.loadHikingRouteDetails(hikingRouteUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    placeDomainUiMapper.mapToHikingRouteDetails(hikingRouteUiModel, geometry)
                )
            }
        }

    @Test
    fun `When loadHikingRouteDetails, then follow location should be disabled`() =
        runTestDefault {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
            coEvery { placesRepository.getGeometry(osmId, any()) } returns geometry

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
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
            coEvery { placesRepository.getGeometry(osmId, any()) } returns geometry
            coEvery { placesRepository.getHikingRoutes(any()) } returns hikingRoutes

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(homeUiModelMapper.mapHikingRoutes(placeName, hikingRoutes))

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
                    oktRoutesMapper.map(DEFAULT_OKT_ROUTES, LOCAL_OKT_ROUTES)
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
                viewModel.saveMapConfig(boundingBox)

                assertThat(awaitItem()).isEqualTo(MapUiModel())
                assertThat(awaitItem()).isEqualTo(MapUiModel(boundingBox))
            }
        }

    @Test
    fun `When clearPlaceDetails, then place details UI model should be cleared`() =
        runTestDefault {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            val geometry = Geometry.Node(placeUiModel.osmId, Location(47.7193842, 18.8962014))
            coEvery { placesRepository.getGeometry(any(), any()) } returns geometry

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetailsWithGeometry(placeUiModel)
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
            coEvery { placesRepository.getHikingRoutes(boundingBox) } returns hikingRoutes

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(homeUiModelMapper.mapHikingRoutes(placeName, hikingRoutes))

                viewModel.clearHikingRoutes()
                assertThat(awaitItem()).isNull()
            }
        }

    private fun mockLandscapes() {
        val landscapes = listOf(DEFAULT_LANDSCAPE)

        every { landscapeInteractor.requestGetLandscapesFlow() } returns flowOf(landscapes)
    }

    private fun mockOktRoutes() {
        coEvery { oktRepository.getOktRoutes() } returns DEFAULT_OKT_ROUTES
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
            address = DEFAULT_NODE_CITY,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.MAP_SEARCH,
        )
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_PLACE.osmId,
            placeType = DEFAULT_PLACE.placeType,
            primaryText = DEFAULT_PLACE.name.toMessage(),
            secondaryText = DEFAULT_PLACE.address.toMessage(),
            iconRes = R.drawable.ic_place_type_node,
            geoPoint = DEFAULT_PLACE.location.toGeoPoint(),
            placeFeature = PlaceFeature.MAP_SEARCH,
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
        private val DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES = BoundingBox(
            north = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH,
            east = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST,
            south = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH,
            west = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
        )
        private val DEFAULT_LANDSCAPE = LOCAL_LANDSCAPES.first()
        private val DEFAULT_OKT_FULL_GEO_POINTS = listOf(
            LOCAL_OKT_ROUTES.first().start,
            LOCAL_OKT_ROUTES.first().end,
            LOCAL_OKT_ROUTES[1].start,
            LOCAL_OKT_ROUTES[1].end,
        )
        private val DEFAULT_OKT_ROUTES = OktRoutes(
            locations = DEFAULT_OKT_FULL_GEO_POINTS,
            stampWaypoints = emptyList(),
        )
    }

}

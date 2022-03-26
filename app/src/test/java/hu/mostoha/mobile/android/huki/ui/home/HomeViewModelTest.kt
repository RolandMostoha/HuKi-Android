package hu.mostoha.mobile.android.huki.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.interactor.HikingLayerInteractor
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.PlacesInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.rule.MainCoroutineRule
import hu.mostoha.mobile.android.huki.rule.runBlockingTest
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import hu.mostoha.mobile.android.huki.util.flowOfError
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.osmdroid.util.GeoPoint
import java.io.File

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    private val layerInteractor = mockk<HikingLayerInteractor>()

    private val placesInteractor = mockk<PlacesInteractor>()

    private val landscapeInteractor = mockk<LandscapeInteractor>()

    private val exceptionLogger = mockk<ExceptionLogger>()

    private val generator = mockk<HomeUiModelGenerator>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        viewModel = spyk(
            HomeViewModel(
                TestTaskExecutor(),
                exceptionLogger,
                layerInteractor,
                placesInteractor,
                landscapeInteractor,
                generator
            )
        )

        every { exceptionLogger.recordException(any()) } returns Unit
    }

    @Test
    fun `Given null hiking layer file, when loadHikingLayer, then loading is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val hikingLayerFile = null
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOf(hikingLayerFile)
            coEvery { generator.generateHikingLayerState(hikingLayerFile) } returns HikingLayerUiModel.NotDownloaded

            viewModel.loading.test {
                viewModel.loadHikingLayer()

                assertThat(awaitItem()).isTrue()
                assertThat(awaitItem()).isFalse()
            }
        }
    }

    @Test
    fun `Given null hiking layer file, when loadHikingLayer, then Loading and NotDownloaded hiking layer is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val hikingLayerFile = null
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOf(hikingLayerFile)
            coEvery { generator.generateHikingLayerState(hikingLayerFile) } returns HikingLayerUiModel.NotDownloaded

            viewModel.hikingLayer.test {
                viewModel.loadHikingLayer()

                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.Loading)
                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.NotDownloaded)
            }
        }
    }

    @Test
    fun `Given valid hiking layer file, when loadHikingLayer, then Loading and NotDownloaded hiking layer is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val hikingLayerFile = File("")
            val hikingLayerUiModel = HikingLayerUiModel.Downloaded(hikingLayerFile, "2022.03.31")
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOf(hikingLayerFile)
            coEvery { generator.generateHikingLayerState(hikingLayerFile) } returns hikingLayerUiModel

            viewModel.hikingLayer.test {
                viewModel.loadHikingLayer()

                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.Loading)
                assertThat(awaitItem()).isEqualTo(hikingLayerUiModel)
            }
        }
    }

    @Test
    fun `Given unknown error, when loadHikingLayer, then error message is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val exception = UnknownException(Exception(""))
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOfError(exception)

            viewModel.errorMessage.test {
                viewModel.loadHikingLayer()

                assertThat(awaitItem()).isEqualTo(R.string.error_message_unknown.toMessage())
            }
        }
    }

    @Test
    fun `When downloadHikingLayer, then hiking layer is in downloading state`() {
        mainCoroutineRule.runBlockingTest {
            val downloadId = 12345L
            coEvery { layerInteractor.requestDownloadHikingLayerFileFlow() } returns flowOf(downloadId)

            viewModel.hikingLayer.test {
                viewModel.downloadHikingLayer()

                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.Loading)
                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.Downloading)
            }
        }
    }

    @Test
    fun `Given error, when downloadHikingLayer, then error message is emitted and request for hiking layer file is initiated`() {
        mainCoroutineRule.runBlockingTest {
            val exception = UnknownException(Exception(""))
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOf(File(""))
            coEvery { layerInteractor.requestDownloadHikingLayerFileFlow() } returns flowOfError(exception)

            viewModel.errorMessage.test {
                viewModel.downloadHikingLayer()

                assertThat(awaitItem()).isEqualTo(R.string.error_message_unknown.toMessage())
            }

            coVerify { layerInteractor.requestHikingLayerFileFlow() }
        }
    }

    @Test
    fun `Given download id, when saveHikingLayer, then Loading and Downloading hiking layer is emitted and request for hiking layer file is initiated`() {
        mainCoroutineRule.runBlockingTest {
            val downloadId = 12345L
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOf(File(""))
            coEvery { layerInteractor.requestSaveHikingLayerFileFlow(downloadId) } returns flowOf(Unit)

            viewModel.hikingLayer.test {
                viewModel.saveHikingLayer(downloadId)

                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.Loading)
                assertThat(awaitItem()).isEqualTo(HikingLayerUiModel.Downloading)
            }

            coVerify { layerInteractor.requestHikingLayerFileFlow() }
        }
    }

    @Test
    fun `Given error, when saveHikingLayer, then error message is emitted and request for hiking layer file is initiated`() {
        mainCoroutineRule.runBlockingTest {
            val downloadId = 12345L
            val exception = UnknownException(Exception(""))
            coEvery { layerInteractor.requestHikingLayerFileFlow() } returns flowOf(File(""))
            coEvery { layerInteractor.requestSaveHikingLayerFileFlow(downloadId) } returns flowOfError(exception)

            viewModel.errorMessage.test {
                viewModel.saveHikingLayer(downloadId)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_unknown.toMessage())
            }

            coVerify { layerInteractor.requestHikingLayerFileFlow() }
        }
    }

    @Test
    fun `Given search text, when loadPlacesBy, then search bar items are emitted`() {
        mainCoroutineRule.runBlockingTest {
            val searchText = "Mecs"
            val places = listOf(DEFAULT_PLACE)
            val searchBarItems = listOf(SearchBarItem.Place(DEFAULT_PLACE_UI_MODEL))
            coEvery { placesInteractor.requestGetPlacesByFlow(searchText) } returns flowOf(places)
            coEvery { generator.generateSearchBarItems(places) } returns searchBarItems

            viewModel.searchBarItems.test {
                viewModel.loadPlacesBy(searchText)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(searchBarItems)
            }
        }
    }

    @Test
    fun `Given error, when loadPlacesBy, then error search bar item is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val errorRes = R.string.error_message_unknown.toMessage()
            val domainException = DomainException(errorRes)
            val searchBarItems = listOf(
                SearchBarItem.Error(
                    messageRes = domainException.messageRes,
                    drawableRes = R.drawable.ic_search_bar_error
                )
            )
            coEvery { placesInteractor.requestGetPlacesByFlow(any()) } returns flowOfError(domainException)
            coEvery { generator.generatePlacesErrorItem(domainException) } returns searchBarItems

            viewModel.searchBarItems.test {
                viewModel.loadPlacesBy("")

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(searchBarItems)
            }
        }
    }

    @Test
    fun `Given place, when loadPlace, then place details is posted`() {
        mainCoroutineRule.runBlockingTest {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            coEvery { generator.generatePlaceDetails(placeUiModel) } returns placeDetailsUiModel

            viewModel.loadPlace(placeUiModel)

            viewModel.placeDetails.test {
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }
    }

    @Test
    fun `Given place, when loadPlaceDetails, then place details is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val osmId = DEFAULT_PLACE.osmId
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
            val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
            coEvery { placesInteractor.requestGeometryFlow(osmId, PlaceType.NODE) } returns flowOf(geometry)
            coEvery { generator.generatePlaceDetails(placeUiModel, geometry) } returns placeDetailsUiModel

            viewModel.placeDetails.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }
    }

    @Test
    fun `Given error, when loadPlaceDetails, then error message is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val placeUiModel = DEFAULT_PLACE_UI_MODEL
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery {
                placesInteractor.requestGeometryFlow(placeUiModel.osmId, placeUiModel.placeType)
            } returns flowOfError(DomainException(errorRes))

            viewModel.errorMessage.test {
                viewModel.loadPlaceDetails(placeUiModel)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }
    }

    @Test
    fun `When loadLandscapes, then landscapes are emitted`() {
        mainCoroutineRule.runBlockingTest {
            val landscapes = listOf(DEFAULT_LANDSCAPE)
            val landscapesUiModels = listOf(DEFAULT_PLACE_UI_MODEL)
            coEvery { landscapeInteractor.requestGetLandscapesFlow() } returns flowOf(landscapes)
            coEvery { generator.generateLandscapes(landscapes) } returns landscapesUiModels

            viewModel.landscapes.test {
                viewModel.loadLandscapes()

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(landscapesUiModels)
            }
        }
    }

    @Test
    fun `Given error, when loadLandscapes, then error message is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery { landscapeInteractor.requestGetLandscapesFlow() } returns flowOfError(DomainException(errorRes))

            viewModel.errorMessage.test {
                viewModel.loadLandscapes()

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }
    }

    @Test
    fun `Given place name and bounding box, when loadHikingRoutes, then hiking routes are emitted`() {
        mainCoroutineRule.runBlockingTest {
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            val hikingRouteUiModels = listOf(HikingRoutesItem.Item(DEFAULT_HIKING_ROUTE_UI_MODEL))
            coEvery { placesInteractor.requestGetHikingRoutesFlow(boundingBox) } returns flowOf(hikingRoutes)
            coEvery { generator.generateHikingRoutes(placeName, hikingRoutes) } returns hikingRouteUiModels

            viewModel.hikingRoutes.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(hikingRouteUiModels)
            }
        }
    }

    @Test
    fun `Given error, when loadHikingRoutes, then error message is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val placeName = DEFAULT_HIKING_ROUTE.name
            val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery {
                placesInteractor.requestGetHikingRoutesFlow(boundingBox)
            } returns flowOfError(DomainException(errorRes))

            viewModel.errorMessage.test {
                viewModel.loadHikingRoutes(placeName, boundingBox)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }
    }

    @Test
    fun `Given hiking route, when loadHikingRouteDetails, then hiking routes are emitted`() {
        mainCoroutineRule.runBlockingTest {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
            val placeDetailsUiModel = DEFAULT_HIKING_ROUTE_DETAILS_UI_MODEL
            coEvery { placesInteractor.requestGeometryFlow(osmId, any()) } returns flowOf(geometry)
            coEvery { generator.generateHikingRouteDetails(hikingRouteUiModel, geometry) } returns placeDetailsUiModel

            viewModel.placeDetails.test {
                viewModel.loadHikingRouteDetails(hikingRouteUiModel)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(placeDetailsUiModel)
            }
        }
    }

    @Test
    fun `Given error, when loadHikingRouteDetails, then error message is emitted`() {
        mainCoroutineRule.runBlockingTest {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val errorRes = R.string.error_message_too_many_requests.toMessage()
            coEvery {
                placesInteractor.requestGeometryFlow(osmId, any())
            } returns flowOfError(DomainException(errorRes))

            viewModel.errorMessage.test {
                viewModel.loadHikingRouteDetails(hikingRouteUiModel)

                assertThat(awaitItem()).isEqualTo(R.string.error_message_too_many_requests.toMessage())
            }
        }
    }

    @Test
    fun `Given unexpected exception, when loadHikingRouteDetails, then exception logger is called`() {
        mainCoroutineRule.runBlockingTest {
            val osmId = DEFAULT_HIKING_ROUTE.osmId
            val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
            val exception = IllegalStateException("Unexpected")
            coEvery {
                placesInteractor.requestGeometryFlow(osmId, any())
            } returns flowOfError(exception)

            viewModel.loadHikingRouteDetails(hikingRouteUiModel)

            verify {
                exceptionLogger.recordException(exception)
            }
        }
    }

    companion object {
        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_PLACE.osmId,
            placeType = DEFAULT_PLACE.placeType,
            primaryText = DEFAULT_PLACE.name,
            secondaryText = null,
            iconRes = 0,
            geoPoint = DEFAULT_PLACE.location.toGeoPoint(),
            boundingBox = null
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
                primaryText = DEFAULT_HIKING_ROUTE.name,
                secondaryText = null,
                iconRes = 0,
                geoPoint = GeoPoint(DEFAULT_HIKING_ROUTE_START_LATITUDE, DEFAULT_HIKING_ROUTE_START_LONGITUDE),
                boundingBox = null
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
        private val DEFAULT_LANDSCAPE = Landscape(
            osmId = DEFAULT_LANDSCAPE_OSM_ID,
            name = DEFAULT_LANDSCAPE_NAME,
            type = LandscapeType.MOUNTAIN_RANGE_HIGH,
            center = Location(DEFAULT_LANDSCAPE_LATITUDE, DEFAULT_LANDSCAPE_LONGITUDE)
        )
    }

}

package hu.mostoha.mobile.android.huki.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.interactor.*
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.osmdroid.util.GeoPoint
import java.io.File

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    private val layerInteractor = mockk<HikingLayerInteractor>()
    private val placesInteractor = mockk<PlacesInteractor>()
    private val landscapeInteractor = mockk<LandscapeInteractor>()
    private val generator = mockk<HomeUiModelGenerator>()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        homeViewModel = spyk(
            HomeViewModel(
                TestTaskExecutor(),
                layerInteractor,
                placesInteractor,
                landscapeInteractor,
                generator
            )
        )
    }

    @Test
    fun `Given null TaskResult, when loadHikingLayer, then Loading and HikingLayerDetails with null layer file posted`() {
        val hikingLayerDetails = HikingLayerDetailsUiModel(
            isHikingLayerFileDownloaded = false,
            hikingLayerFile = null,
            lastUpdatedText = null
        )
        coEvery { layerInteractor.requestGetHikingLayerFile() } returns TaskResult.Success(null)
        coEvery { generator.generateHikingLayerDetails(null) } returns hikingLayerDetails

        homeViewModel.loadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayerFile()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(hikingLayerDetails))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingLayer, then Loading and ErrorOccurred posted`() {
        val errorRes = R.string.error_message_unknown.toMessage()
        coEvery {
            layerInteractor.requestGetHikingLayerFile()
        } returns TaskResult.Error(DomainException(messageRes = errorRes))

        homeViewModel.loadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayerFile()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when downloadHikingLayer, then Loading posted`() {
        val requestId: Long = 12345
        coEvery { layerInteractor.requestDownloadHikingLayerFile() } returns TaskResult.Success(requestId)

        homeViewModel.downloadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayerFile()
        }
    }

    @Test
    fun `Given Error TaskResult, when downloadHikingLayer, then ErrorOccurred posted`() {
        val errorRes = R.string.error_message_unknown.toMessage()
        coEvery {
            layerInteractor.requestDownloadHikingLayerFile()
        } returns TaskResult.Error(DomainException(messageRes = errorRes))

        homeViewModel.downloadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayerFile()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadDownloadedFile, then HomeViewState posted`() {
        val downloadId: Long = 12345
        val file = File("path")
        val hikingLayerDetails = HikingLayerDetailsUiModel(
            isHikingLayerFileDownloaded = false,
            hikingLayerFile = null,
            lastUpdatedText = null
        )
        coEvery { layerInteractor.requestSaveHikingLayerFile(downloadId) } returns TaskResult.Success(Unit)
        coEvery { layerInteractor.requestGetHikingLayerFile() } returns TaskResult.Success(file)
        coEvery { generator.generateHikingLayerDetails(any()) } returns hikingLayerDetails

        homeViewModel.loadDownloadedFile(downloadId)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayerFile(downloadId)
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(hikingLayerDetails))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadDownloadedFile, then ErrorOccurred posted`() {
        val downloadId = 1L
        val errorRes = R.string.download_layer_missing_downloaded_file.toMessage()
        coEvery {
            layerInteractor.requestSaveHikingLayerFile(downloadId)
        } returns TaskResult.Error(DomainException(messageRes = errorRes))

        homeViewModel.loadDownloadedFile(downloadId)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayerFile(downloadId)
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadPlacesBy, then PlacesResult posted`() {
        val searchText = "Mecs"
        val places = listOf(DEFAULT_PLACE)
        val searchBarItems = listOf(SearchBarItem.Place(DEFAULT_PLACE_UI_MODEL))
        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(places)
        coEvery { generator.generatePlaceAdapterItems(places) } returns searchBarItems

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaceAdapterItems(places)
            homeViewModel.postEvent(HomeLiveEvents.PlacesResult(searchBarItems))
        }
    }

    @Test
    fun `Given empty TaskResult, when loadPlacesBy, then PlacesResultError posted`() {
        val searchText = "Mecs"
        val predictions = emptyList<Place>()
        val placeUiModels = emptyList<SearchBarItem.Place>()
        val searchBarErrorItem = SearchBarItem.Error(
            messageRes = R.string.search_bar_empty_message.toMessage(),
            drawableRes = R.drawable.ic_search_bar_empty_result
        )
        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(predictions)
        coEvery { generator.generatePlaceAdapterItems(any()) } returns placeUiModels
        coEvery { generator.generatePlacesEmptyItem() } returns searchBarErrorItem

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaceAdapterItems(predictions)
            homeViewModel.postEvent(HomeLiveEvents.PlacesErrorResult(searchBarErrorItem))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadPlacesBy, then PlacesErrorResult posted`() {
        val errorRes = R.string.error_message_unknown.toMessage()
        val domainException = DomainException(messageRes = errorRes)
        val searchBarErrorItem = SearchBarItem.Error(
            messageRes = domainException.messageRes,
            drawableRes = R.drawable.ic_search_bar_error
        )
        coEvery { placesInteractor.requestGetPlacesBy(any()) } returns TaskResult.Error(domainException)
        coEvery { generator.generatePlacesErrorItem(domainException) } returns searchBarErrorItem

        homeViewModel.loadPlacesBy("")

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.PlacesErrorResult(searchBarErrorItem))
        }
    }

    @Test
    fun `Given place, when loadPlace, then PlaceResult posted`() {
        val place = DEFAULT_PLACE_UI_MODEL

        homeViewModel.loadPlace(place)

        coVerifyOrder { homeViewModel.postEvent(HomeLiveEvents.PlaceResult(place)) }
    }

    @Test
    fun `Given Success TaskResult, when loadPlaceDetails, then PlaceDetailsResult posted`() {
        val osmId = DEFAULT_PLACE.osmId
        val placeUiModel = DEFAULT_PLACE_UI_MODEL
        val placeDetailsUiModel = DEFAULT_PLACE_DETAILS_UI_MODEL
        val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
        coEvery { placesInteractor.requestGetGeometry(osmId, PlaceType.NODE) } returns TaskResult.Success(geometry)
        coEvery { generator.generatePlaceDetails(placeUiModel, geometry) } returns placeDetailsUiModel

        homeViewModel.loadPlaceDetails(placeUiModel)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(osmId, DEFAULT_PLACE_UI_MODEL.placeType)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaceDetails(placeUiModel, geometry)
            homeViewModel.postEvent(HomeLiveEvents.PlaceDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadPlaceDetails, then ErrorOccurred posted`() {
        val placeUiModel = DEFAULT_PLACE_UI_MODEL
        val errorRes = R.string.error_message_unknown.toMessage()
        coEvery { placesInteractor.requestGetGeometry(any(), any()) } returns TaskResult.Error(
            DomainException(messageRes = errorRes)
        )

        homeViewModel.loadPlaceDetails(placeUiModel)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadLandscapes, then LandscapesResult posted`() {
        val landscapes = listOf(DEFAULT_LANDSCAPE)
        val landscapesUiModels = listOf(DEFAULT_PLACE_UI_MODEL)
        coEvery { landscapeInteractor.requestGetLandscapes() } returns TaskResult.Success(landscapes)
        coEvery { generator.generateLandscapes(landscapes) } returns landscapesUiModels

        homeViewModel.loadLandscapes()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            landscapeInteractor.requestGetLandscapes()
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateLandscapes(landscapes)
            homeViewModel.postEvent(HomeLiveEvents.LandscapesResult(landscapesUiModels))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadLandscapes, then ErrorOccurred posted`() {
        val errorRes = R.string.error_message_unknown.toMessage()
        coEvery {
            landscapeInteractor.requestGetLandscapes()
        } returns TaskResult.Error(DomainException(messageRes = errorRes))

        homeViewModel.loadLandscapes()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            landscapeInteractor.requestGetLandscapes()
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadHikingRoutes, then HikingRoutesResult posted`() {
        val placeName = DEFAULT_HIKING_ROUTE.name
        val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
        val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
        val hikingRoutesUiModel = listOf(HikingRoutesItem.Item(DEFAULT_HIKING_ROUTE_UI_MODEL))
        coEvery { placesInteractor.requestGetHikingRoutes(boundingBox) } returns TaskResult.Success(hikingRoutes)
        coEvery { generator.generateHikingRoutes(placeName, hikingRoutes) } returns hikingRoutesUiModel

        homeViewModel.loadHikingRoutes(placeName, boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetHikingRoutes(boundingBox)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateHikingRoutes(placeName, hikingRoutes)
            homeViewModel.postEvent(HomeLiveEvents.HikingRoutesResult(hikingRoutesUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRoutes, then ErrorOccurred posted`() {
        val boundingBox = DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES
        val errorRes = R.string.error_message_unknown.toMessage()
        coEvery {
            placesInteractor.requestGetHikingRoutes(boundingBox)
        } returns TaskResult.Error(DomainException(messageRes = errorRes))

        homeViewModel.loadHikingRoutes("Bükk", boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetHikingRoutes(boundingBox)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadHikingRouteDetails, then PlaceDetailsResult posted`() {
        val osmId = DEFAULT_HIKING_ROUTE.osmId
        val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
        val geometry = DEFAULT_HIKING_ROUTE_GEOMETRY
        val placeDetailsUiModel = DEFAULT_HIKING_ROUTE_DETAILS_UI_MODEL
        coEvery { placesInteractor.requestGetGeometry(osmId, any()) } returns TaskResult.Success(geometry)
        coEvery { generator.generateHikingRouteDetails(hikingRouteUiModel, geometry) } returns placeDetailsUiModel

        homeViewModel.loadHikingRouteDetails(hikingRouteUiModel)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(osmId, PlaceType.RELATION)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateHikingRouteDetails(hikingRouteUiModel, geometry)
            homeViewModel.postEvent(HomeLiveEvents.HikingRouteDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRouteDetails, then ErrorOccurred posted`() {
        val hikingRouteUiModel = DEFAULT_HIKING_ROUTE_UI_MODEL
        val errorRes = R.string.error_message_unknown.toMessage()
        coEvery {
            placesInteractor.requestGetGeometry(any(), any())
        } returns TaskResult.Error(DomainException(messageRes = errorRes))

        homeViewModel.loadHikingRouteDetails(hikingRouteUiModel)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorResult(errorRes))
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

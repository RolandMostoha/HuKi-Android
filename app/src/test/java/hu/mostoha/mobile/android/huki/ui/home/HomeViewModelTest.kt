package hu.mostoha.mobile.android.huki.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.interactor.*
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
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

    private val layerInteractor = mockk<LayerInteractor>()
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
    fun `Given null TaskResult, when loadHikingLayer, then Loading and null layer file posted`() {
        coEvery { layerInteractor.requestGetHikingLayer() } returns TaskResult.Success(null)
        coEvery { generator.generateHikingLayerDetails(any()) } returns HikingLayerDetailsUiModel(false, null, null)

        homeViewModel.loadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(HikingLayerDetailsUiModel(false, null, null)))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingLayer, then Loading and ErrorOccurred posted`() {
        val errorRes = R.string.error_message_unknown
        coEvery { layerInteractor.requestGetHikingLayer() } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.loadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when downloadHikingLayer, then Loading posted`() {
        val requestId: Long = 12345
        coEvery { layerInteractor.requestDownloadHikingLayer() } returns TaskResult.Success(requestId)

        homeViewModel.downloadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayer()
        }
    }

    @Test
    fun `Given Error TaskResult, when downloadHikingLayer, then ErrorOccurred posted`() {
        val errorRes = R.string.error_message_unknown
        coEvery { layerInteractor.requestDownloadHikingLayer() } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.downloadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayer()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadDownloadedFile, then HomeViewState posted`() {
        val downloadId: Long = 12345
        val file = File("path")
        coEvery { layerInteractor.requestSaveHikingLayer(downloadId) } returns TaskResult.Success(Unit)
        coEvery { layerInteractor.requestGetHikingLayer() } returns TaskResult.Success(file)
        coEvery { generator.generateHikingLayerDetails(any()) } returns HikingLayerDetailsUiModel(false, null, null)

        homeViewModel.loadDownloadedFile(downloadId)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayer(downloadId)
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(HikingLayerDetailsUiModel(false, null, null)))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadDownloadedFile, then ErrorOccurred posted`() {
        val downloadId = 1L
        val errorRes = R.string.download_layer_missing_downloaded_file
        coEvery {
            layerInteractor.requestSaveHikingLayer(downloadId)
        } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.loadDownloadedFile(downloadId)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayer(downloadId)
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadPlacesBy, then PlacesResult posted`() {
        val searchText = "Mecs"
        val predictions = listOf("Mecsek".toTestPlace(), "Mecsek utca".toTestPlace())
        val placeUiModels = listOf("Mecsek".toTestPlaceUiModel(), "Mecsek utca".toTestPlaceUiModel())
        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(predictions)
        coEvery { generator.generatePlaces(any()) } returns placeUiModels

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaces(predictions)
            homeViewModel.postEvent(HomeLiveEvents.PlacesResult(placeUiModels))
        }
    }

    @Test
    fun `Given empty TaskResult, when loadPlacesBy, then PlacesResultError posted`() {
        val searchText = "Mecs"
        val predictions = emptyList<Place>()
        val placeUiModels = emptyList<PlaceUiModel>()
        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(predictions)
        coEvery { generator.generatePlaces(any()) } returns placeUiModels

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaces(predictions)
            homeViewModel.postEvent(
                HomeLiveEvents.PlacesErrorResult(
                    R.string.search_bar_empty_message, R.drawable.ic_search_bar_empty_result
                )
            )
        }
    }

    @Test
    fun `Given Error TaskResult, when loadPlacesBy, then PlacesErrorResult posted`() {
        val errorRes = R.string.error_message_unknown
        coEvery { placesInteractor.requestGetPlacesBy(any()) } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.loadPlacesBy("")

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(
                HomeLiveEvents.PlacesErrorResult(
                    R.string.error_message_unknown, R.drawable.ic_search_bar_error
                )
            )
        }
    }

    @Test
    fun `Given place, when loadPlace, then PlaceResult posted`() {
        val place = "Mecsek".toTestPlaceUiModel()

        homeViewModel.loadPlace(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.PlaceResult(place))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadPlaceDetails, then PlaceDetailsResult posted`() {
        val osmId = "1234567"
        val placeName = "Mecsek"
        val place = placeName.toTestPlaceUiModel(osmId)
        val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
        val placeDetailsUiModel = placeName.toTestPlaceDetailsUiModel()
        coEvery { placesInteractor.requestGetGeometry(osmId, any()) } returns TaskResult.Success(geometry)
        coEvery { generator.generatePlaceDetails(any(), any()) } returns placeDetailsUiModel

        homeViewModel.loadPlaceDetails(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(osmId, any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaceDetails(place, geometry)
            homeViewModel.postEvent(HomeLiveEvents.PlaceDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadPlaceDetails, then ErrorOccurred posted`() {
        val place = "Gerecse".toTestPlaceUiModel()
        val errorRes = R.string.error_message_unknown
        coEvery { placesInteractor.requestGetGeometry(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadPlaceDetails(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadLandscapes, then LandscapesResult posted`() {
        val landscapes = emptyList<Landscape>()
        val landscapesUiModels = listOf("Gerecse".toTestPlaceUiModel())
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
        val errorRes = R.string.error_message_unknown
        coEvery { landscapeInteractor.requestGetLandscapes() } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadLandscapes()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            landscapeInteractor.requestGetLandscapes()
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadHikingRoutes, then HikingRoutesResult posted`() {
        val boundingBox = BoundingBox(48.0058358, 20.2007937, 47.7747357, 19.6898570)
        val hikingRoutes = emptyList<HikingRoute>()
        val hikingRoutesUiModel = listOf(HikingRoutesItem.Item(HikingRouteUiModel("1", "HikingRoute", 0)))
        val placeName = "Bükk"
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
        val boundingBox = BoundingBox(48.0058358, 20.2007937, 47.7747357, 19.6898570)
        val errorRes = R.string.error_message_unknown
        val placeName = "Bükk"
        coEvery { placesInteractor.requestGetHikingRoutes(boundingBox) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadHikingRoutes(placeName, boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetHikingRoutes(boundingBox)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadHikingRouteDetails, then PlaceDetailsResult posted`() {
        val osmId = "1"
        val hikingRouteName = "Z+ János hegyi túra"
        val hikingRoute = HikingRouteUiModel(osmId, hikingRouteName, 0)
        val geometry = Geometry.Node(osmId, Location(47.7193842, 18.8962014))
        val placeDetailsUiModel = hikingRouteName.toTestPlaceDetailsUiModel()
        coEvery { placesInteractor.requestGetGeometry(osmId, any()) } returns TaskResult.Success(geometry)
        coEvery { generator.generateHikingRouteDetails(hikingRoute, geometry) } returns placeDetailsUiModel

        homeViewModel.loadHikingRouteDetails(hikingRoute)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(osmId, PlaceType.RELATION)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateHikingRouteDetails(hikingRoute, geometry)
            homeViewModel.postEvent(HomeLiveEvents.HikingRouteDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRouteDetails, then ErrorOccurred posted`() {
        val hikingRoute = HikingRouteUiModel("1", "HikingRoute", 0)
        val errorRes = R.string.error_message_unknown
        coEvery { placesInteractor.requestGetGeometry(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadHikingRouteDetails(hikingRoute)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGeometry(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    private fun String.toTestPlace(osmId: String? = null): Place {
        return Place(
            osmId = osmId ?: "130074457",
            name = this,
            placeType = PlaceType.NODE,
            location = Location(47.7193842, 18.8962014)
        )
    }

    private fun String.toTestPlaceUiModel(osmId: String? = null): PlaceUiModel {
        return PlaceUiModel(
            osmId = osmId ?: "130074457",
            placeType = PlaceType.NODE,
            primaryText = this,
            secondaryText = this.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(47.7193842, 18.8962014),
            boundingBox = null
        )
    }

    private fun String.toTestPlaceDetailsUiModel(): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            placeUiModel = this.toTestPlaceUiModel(),
            geometryUiModel = GeometryUiModel.Node(GeoPoint(47.123, 19.123))
        )
    }

}

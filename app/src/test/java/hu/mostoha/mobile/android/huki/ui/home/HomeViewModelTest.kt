package hu.mostoha.mobile.android.huki.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.interactor.*
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.utils.toMessage
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
        val errorRes = R.string.default_error_message_unknown
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
        val errorRes = R.string.default_error_message_unknown
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
    fun `Given Success TaskResult, when loadPlacesBy, then PlacesResult posted`() {
        val searchText = "Mecs"
        val predictions = listOf("Mecsek".testPrediction(), "Mecsek utca".testPrediction())
        val placeUiModels = listOf("Mecsek".testPlaceUiModel())

        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(predictions)
        coEvery { generator.generatePlacesResult(any()) } returns placeUiModels

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlacesResult(predictions)
            homeViewModel.postEvent(HomeLiveEvents.PlacesResult(placeUiModels))
        }
    }

    @Test
    fun `Given empty TaskResult, when loadPlacesBy, then PlacesResultError posted`() {
        val searchText = "Mecs"
        val predictions = emptyList<PlacePrediction>()
        val placeUiModels = emptyList<PlaceUiModel>()

        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(predictions)
        coEvery { generator.generatePlacesResult(any()) } returns placeUiModels

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlacesResult(predictions)
            homeViewModel.postEvent(
                HomeLiveEvents.PlacesErrorResult(
                    R.string.search_bar_empty_message, R.drawable.ic_search_bar_empty_result
                )
            )
        }
    }

    @Test
    fun `Given Error TaskResult, when loadPlacesBy, then PlacesErrorResult posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetPlacesBy(any()) } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.loadPlacesBy("")

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(
                HomeLiveEvents.PlacesErrorResult(
                    R.string.default_error_message_unknown, R.drawable.ic_search_bar_error
                )
            )
        }
    }

    @Test
    fun `Given Success TaskResult, when loadPlaceDetails, then PlaceDetailsResult posted`() {
        val id = "123456L"
        val place = PlaceUiModel(id, PlaceType.NODE, "", "", 0)
        val placeDetails = PlaceDetails(id, PayLoad.Node(Location(47.123, 19.123)))
        val placeDetailsUiModel = "Mecsek".testPlaceDetailsUiModel()

        coEvery { placesInteractor.requestGetPlaceDetails(id, any()) } returns TaskResult.Success(placeDetails)
        coEvery { generator.generatePlaceDetails(any(), any()) } returns placeDetailsUiModel

        homeViewModel.loadPlaceDetails(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlaceDetails(id, any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaceDetails(place, placeDetails)
            homeViewModel.postEvent(HomeLiveEvents.PlaceDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadPlaceDetails, then ErrorOccurred posted`() {
        val place = PlaceUiModel("123456L", PlaceType.NODE, "", "", 0)
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetPlaceDetails(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadPlaceDetails(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlaceDetails(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadLandscapes, then LandscapesResult posted`() {
        val landscapes = emptyList<Landscape>()
        val landscapesUiModel = listOf(PlaceUiModel("1", PlaceType.NODE, "", "", 0))

        coEvery { landscapeInteractor.requestGetLandscapes() } returns TaskResult.Success(landscapes)
        coEvery { generator.generateLandscapes(landscapes) } returns landscapesUiModel

        homeViewModel.loadLandscapes()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            landscapeInteractor.requestGetLandscapes()
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateLandscapes(landscapes)
            homeViewModel.postEvent(HomeLiveEvents.LandscapesResult(landscapesUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadLandscapes, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
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

        coEvery { placesInteractor.requestGetHikingRoutes(boundingBox) } returns TaskResult.Success(hikingRoutes)
        coEvery { generator.generateHikingRoutes("Bükk", hikingRoutes) } returns hikingRoutesUiModel

        homeViewModel.loadHikingRoutes("Bükk", boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetHikingRoutes(boundingBox)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateHikingRoutes("Bükk", hikingRoutes)
            homeViewModel.postEvent(HomeLiveEvents.HikingRoutesResult(hikingRoutesUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRoutes, then ErrorOccurred posted`() {
        val boundingBox = BoundingBox(48.0058358, 20.2007937, 47.7747357, 19.6898570)
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetHikingRoutes(boundingBox) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadHikingRoutes("Bükk", boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetHikingRoutes(boundingBox)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadHikingRouteDetails, then PlaceDetailsResult posted`() {
        val id = "1"
        val hikingRoute = HikingRouteUiModel(id, "HikingRoute", 0)
        val placeDetails = PlaceDetails(id, PayLoad.Node(Location(47.123, 19.123)))
        val placeDetailsUiModel = "HikingRoute".testPlaceDetailsUiModel()

        coEvery { placesInteractor.requestGetPlaceDetails(id, any()) } returns TaskResult.Success(placeDetails)
        coEvery { generator.generateHikingRouteDetails(hikingRoute, placeDetails) } returns placeDetailsUiModel

        homeViewModel.loadHikingRouteDetails(hikingRoute)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlaceDetails(id, PlaceType.RELATION)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateHikingRouteDetails(hikingRoute, placeDetails)
            homeViewModel.postEvent(HomeLiveEvents.HikingRouteDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRouteDetails, then ErrorOccurred posted`() {
        val hikingRoute = HikingRouteUiModel("1", "HikingRoute", 0)
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetPlaceDetails(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadHikingRouteDetails(hikingRoute)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlaceDetails(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

}

private fun String.testPrediction(): PlacePrediction {
    return PlacePrediction("123456", PlaceType.NODE, this, null)
}

private fun String.testPlaceUiModel(): PlaceUiModel {
    return PlaceUiModel("123456", PlaceType.NODE, this, null, 0)
}

private fun String.testPlaceDetailsUiModel(): PlaceDetailsUiModel {
    return PlaceDetailsUiModel("123456", this.testPlaceUiModel(), UiPayLoad.Node(GeoPoint(47.123, 19.123)))
}



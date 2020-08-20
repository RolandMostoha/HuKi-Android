package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.executor.TestTaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.*
import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.turistautak.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.UiPayLoad
import hu.mostoha.mobile.android.turistautak.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.turistautak.ui.utils.toMessage
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

        homeViewModel.loadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer()
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(null))
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
    fun `Given Success TaskResult, when loadDownloadedFile, then Loading and layer file posted`() {
        val downloadId: Long = 12345
        val file = File("path")
        coEvery { layerInteractor.requestSaveHikingLayer(downloadId) } returns TaskResult.Success(Unit)
        coEvery { layerInteractor.requestGetHikingLayer() } returns TaskResult.Success(file)

        homeViewModel.loadDownloadedFile(downloadId)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayer(downloadId)
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(file))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadHikingRelationsBy, then SearchResult posted`() {
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
    fun `Given Error TaskResult, when loadHikingRelationsBy, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetPlacesBy(any()) } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.loadPlacesBy("")

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes.toMessage()))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadRelation, then NodesResult posted`() {
        val id = "123456L"
        val place = PlaceUiModel(id, PlaceType.NODE, "", "", 0)
        val placeDetails = PlaceDetails(id, PayLoad.Node(Location(47.123, 19.123)))
        val placeDetailsUiModel = "Mecsek".testPlaceDetailsUiModel()

        coEvery { placesInteractor.requestGetGetPlaceDetails(id, any()) } returns TaskResult.Success(placeDetails)
        coEvery { generator.generatePlaceDetails(any(), any()) } returns placeDetailsUiModel

        homeViewModel.loadPlaceDetails(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGetPlaceDetails(id, any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generatePlaceDetails(place, placeDetails)
            homeViewModel.postEvent(HomeLiveEvents.PlaceDetailsResult(placeDetailsUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadRelation, then ErrorOccurred posted`() {
        val place = PlaceUiModel("123456L", PlaceType.NODE, "", "", 0)
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetGetPlaceDetails(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadPlaceDetails(place)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGetPlaceDetails(any(), any())
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

        coEvery { placesInteractor.requestGetGetHikingRoutes(boundingBox) } returns TaskResult.Success(hikingRoutes)
        coEvery { generator.generateHikingRoutes("Bükk", hikingRoutes) } returns hikingRoutesUiModel

        homeViewModel.loadHikingRoutes("Bükk", boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGetHikingRoutes(boundingBox)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            generator.generateHikingRoutes("Bükk", hikingRoutes)
            homeViewModel.postEvent(HomeLiveEvents.HikingRoutesResult(hikingRoutesUiModel))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRoutes, then ErrorOccurred posted`() {
        val boundingBox = BoundingBox(48.0058358, 20.2007937, 47.7747357, 19.6898570)
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetGetHikingRoutes(boundingBox) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadHikingRoutes("Bükk", boundingBox)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGetHikingRoutes(boundingBox)
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



package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.executor.TestTaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.DomainException
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.PlacesInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.model.domain.Location
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceDetails
import hu.mostoha.mobile.android.turistautak.model.domain.PlacePrediction
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.File
import java.util.*


@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    private val layerInteractor = mockk<LayerInteractor>()
    private val placesInteractor = mockk<PlacesInteractor>()
    private val generator = HomeUiModelGenerator()
    private val landscapeRepository = LandscapeRepository()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        homeViewModel = spyk(
            HomeViewModel(
                TestTaskExecutor(),
                layerInteractor,
                placesInteractor,
                landscapeRepository,
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
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
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
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
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

        coEvery { placesInteractor.requestGetPlacesBy(searchText) } returns TaskResult.Success(predictions)

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetPlacesBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.PlacesResult(generator.generatePlacesResult(predictions)))
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
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadRelation, then NodesResult posted`() {
        val id = "123456L"
        val placeDetails = PlaceDetails(id, Location(47.123, 19.123))

        coEvery { placesInteractor.requestGetGetPlaceDetails(id, any()) } returns TaskResult.Success(placeDetails)

        homeViewModel.loadPlaceDetails(id, PlaceType.NODE)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGetPlaceDetails(id, any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.PlaceDetailsResult(generator.generatePlaceDetails(placeDetails)))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadRelation, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { placesInteractor.requestGetGetPlaceDetails(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadPlaceDetails("123456L", PlaceType.NODE)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            placesInteractor.requestGetGetPlaceDetails(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

}

private fun String.testPrediction(): PlacePrediction {
    return PlacePrediction(UUID.randomUUID().toString(), PlaceType.NODE, this, null)
}



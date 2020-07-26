package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.executor.TestTaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.DomainException
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.OverpassInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.network.model.Element
import hu.mostoha.mobile.android.turistautak.network.model.OverpassQueryResult
import hu.mostoha.mobile.android.turistautak.network.model.Tags
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.SearchBarUiModelGenerator
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


@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    private val layerInteractor = mockk<LayerInteractor>()
    private val overpassInteractor = mockk<OverpassInteractor>()
    private val generator =
        SearchBarUiModelGenerator()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        homeViewModel = spyk(HomeViewModel(TestTaskExecutor(), layerInteractor, overpassInteractor, generator))
    }

    @Test
    fun `Given null TaskResult, when checkHikingLayer, then Loading and null layer file posted`() {
        coEvery { layerInteractor.requestGetHikingLayer(any()) } returns TaskResult.Success(null)

        homeViewModel.checkHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer(any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(null))
        }
    }

    @Test
    fun `Given Error TaskResult, when checkHikingLayer, then Loading and ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { layerInteractor.requestGetHikingLayer(any()) } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.checkHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer(any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when downloadHikingLayer, then Loading posted`() {
        val requestId: Long = 12345
        coEvery { layerInteractor.requestDownloadHikingLayer(any()) } returns TaskResult.Success(requestId)

        homeViewModel.downloadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayer(any())
        }
    }

    @Test
    fun `Given Error TaskResult, when downloadHikingLayer, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { layerInteractor.requestDownloadHikingLayer(any()) } returns TaskResult.Error(DomainException(errorRes))

        homeViewModel.downloadHikingLayer()

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayer(any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when handleFileDownloaded, then Loading and layer file posted`() {
        val downloadId: Long = 12345
        val file = File("path")
        coEvery { layerInteractor.requestSaveHikingLayer(downloadId, any()) } returns TaskResult.Success(Unit)
        coEvery { layerInteractor.requestGetHikingLayer(any()) } returns TaskResult.Success(file)

        homeViewModel.handleFileDownloaded(downloadId)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayer(downloadId, any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(file))
        }
    }

    @Test
    fun `Given Success TaskResult, when searchHikingRelationsBy, then SearchResult posted`() {
        val searchText = "Mecs"

        val overpassQueryResult = OverpassQueryResult(
            listOf(
                Element(type = "route", id = 1, tags = Tags("Mecseki Kéktúra", jel = "k")),
                Element(type = "route", id = 2, tags = Tags("Mecseknádasdi Piroska", jel = "p"))
            )
        )
        coEvery { overpassInteractor.requestSearchHikingRelationsBy(searchText, any()) } returns TaskResult.Success(
            overpassQueryResult
        )

        homeViewModel.searchHikingRelationsBy(searchText)

        coVerifyOrder {
            overpassInteractor.requestSearchHikingRelationsBy(searchText, any())
            val uiResults = generator.generate(overpassQueryResult.elements)
            homeViewModel.postEvent(HomeLiveEvents.SearchResult(uiResults))
        }
    }

    @Test
    fun `Given Error TaskResult, when searchHikingRelationsBy, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { overpassInteractor.requestSearchHikingRelationsBy(any(), any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.searchHikingRelationsBy("")

        coVerifyOrder {
            overpassInteractor.requestSearchHikingRelationsBy(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

}


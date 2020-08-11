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
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.HomeUiModelGenerator
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
    private val landscapeRepository = LandscapeRepository()
    private val generator = HomeUiModelGenerator()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        homeViewModel = spyk(
            HomeViewModel(
                TestTaskExecutor(),
                layerInteractor,
                overpassInteractor,
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

        val overpassQueryResult = OverpassQueryResult(
            listOf(
                Element(type = "route", id = 1, tags = Tags("Mecseki Kéktúra", jel = "k")),
                Element(type = "route", id = 2, tags = Tags("Mecseknádasdi Piroska", jel = "p"))
            )
        )
        coEvery { overpassInteractor.requestSearchHikingRelationsBy(searchText) } returns TaskResult.Success(
            overpassQueryResult
        )

        homeViewModel.loadPlacesBy(searchText)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            overpassInteractor.requestSearchHikingRelationsBy(searchText)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.SearchResult(generator.generateSearchResult(overpassQueryResult.elements)))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadHikingRelationsBy, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { overpassInteractor.requestSearchHikingRelationsBy(any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadPlacesBy("")

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            overpassInteractor.requestSearchHikingRelationsBy(any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when loadRelation, then NodesResult posted`() {
        val id = 123456L

        val overpassQueryResult = OverpassQueryResult(
            listOf(
                Element(type = "node", id = 25287545, lat = 46.1314556, lon = 18.2565377),
                Element(type = "node", id = 25287546, lat = 46.1264344, lon = 18.2650645),
                Element(type = "node", id = 25287547, lat = 46.1360740, lon = 18.2532182)
            )
        )
        coEvery { overpassInteractor.requestGetNodesByRelationId(id) } returns TaskResult.Success(
            overpassQueryResult
        )

        homeViewModel.loadRelation(id)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            overpassInteractor.requestGetNodesByRelationId(id)
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.NodesResult(generator.generateNodes(overpassQueryResult.elements)))
        }
    }

    @Test
    fun `Given Error TaskResult, when loadRelation, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        coEvery { overpassInteractor.requestGetNodesByRelationId(any()) } returns TaskResult.Error(
            DomainException(errorRes)
        )

        homeViewModel.loadRelation(123456L)

        coVerifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(true))
            overpassInteractor.requestGetNodesByRelationId(any())
            homeViewModel.postEvent(HomeLiveEvents.SearchBarLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

}


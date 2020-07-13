package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.interactor.DomainException
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.File


class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    private val layerInteractor = mockk<LayerInteractor>()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        homeViewModel = spyk(HomeViewModel(layerInteractor))
    }

    @Test
    fun `Given null TaskResult, when checkHikingLayer, then Loading and null layer file posted`() {
        every { layerInteractor.requestGetHikingLayer(any(), any()) } answers {
            secondArg<(TaskResult<File?>) -> Unit>().invoke(TaskResult.Success(null))
        }

        homeViewModel.checkHikingLayer()

        verifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(null))
        }
    }

    @Test
    fun `Given Error TaskResult, when checkHikingLayer, then Loading and ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        every { layerInteractor.requestGetHikingLayer(any(), any()) } answers {
            secondArg<(TaskResult<File?>) -> Unit>().invoke(
                TaskResult.Error(DomainException(errorRes))
            )
        }

        homeViewModel.checkHikingLayer()

        verifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestGetHikingLayer(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when downloadHikingLayer, then Loading posted`() {
        val requestId: Long = 12345
        every { layerInteractor.requestDownloadHikingLayer(any(), any()) } answers {
            secondArg<(TaskResult<Long>) -> Unit>().invoke(TaskResult.Success(requestId))
        }

        homeViewModel.downloadHikingLayer()

        verifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayer(any(), any())
        }
    }

    @Test
    fun `Given Error TaskResult, when downloadHikingLayer, then ErrorOccurred posted`() {
        val errorRes = R.string.default_error_message_unknown
        every { layerInteractor.requestDownloadHikingLayer(any(), any()) } answers {
            secondArg<(TaskResult<File?>) -> Unit>().invoke(
                TaskResult.Error(DomainException(errorRes))
            )
        }

        homeViewModel.downloadHikingLayer()

        verifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestDownloadHikingLayer(any(), any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postEvent(HomeLiveEvents.ErrorOccurred(errorRes))
        }
    }

    @Test
    fun `Given Success TaskResult, when handleFileDownloaded, then Loading and layer file posted`() {
        val downloadId: Long = 12345
        val file = File("path")
        every { layerInteractor.requestSaveHikingLayer(downloadId, any(), any()) } answers {
            thirdArg<(TaskResult<Unit>) -> Unit>().invoke(TaskResult.Success(Unit))
        }
        every { layerInteractor.requestGetHikingLayer(any(), any()) } answers {
            secondArg<(TaskResult<File?>) -> Unit>().invoke(TaskResult.Success(file))
        }

        homeViewModel.handleFileDownloaded(downloadId)

        verifyOrder {
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(true))
            layerInteractor.requestSaveHikingLayer(downloadId, any(), any())
            homeViewModel.postEvent(HomeLiveEvents.LayerLoading(false))
            homeViewModel.postState(HomeViewState(file))
        }
    }

}


package hu.mostoha.mobile.android.huki.interactor

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HikingLayerInteractorTest {

    private lateinit var hikingLayerInteractor: HikingLayerInteractor

    private val hikingLayerRepository = mockk<HikingLayerRepository>()

    private val exceptionLogger = mockk<ExceptionLogger>()

    @Before
    fun setUp() {
        hikingLayerInteractor = HikingLayerInteractor(exceptionLogger, hikingLayerRepository)

        every { exceptionLogger.recordException(any()) } returns Unit
    }

    @Test
    fun `Given tile zoom range list, when requestHikingLayerZoomRanges, then zoom ranges are emitted`() {
        runTest {
            val zoomRanges = listOf(TileZoomRange(10, 50, 100, 30, 70))
            coEvery { hikingLayerRepository.getHikingLayerZoomRanges() } returns zoomRanges

            val flow = hikingLayerInteractor.requestHikingLayerZoomRanges()

            flow.test {
                assertThat(awaitItem()).isEqualTo(zoomRanges)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestHikingLayerFileFlow, then unknown domain exception is emitted`() {
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.getHikingLayerZoomRanges() } throws exception

            val flow = hikingLayerInteractor.requestHikingLayerZoomRanges()

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

}

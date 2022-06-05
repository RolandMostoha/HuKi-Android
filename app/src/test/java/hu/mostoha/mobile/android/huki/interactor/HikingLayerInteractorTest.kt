package hu.mostoha.mobile.android.huki.interactor

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

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
    fun `Given hiking layer file, when requestHikingLayerFileFlow, then file is emitted`() {
        runTest {
            val expectedFile = File("pathName")
            coEvery { hikingLayerRepository.getHikingLayerFile() } returns expectedFile

            val flow = hikingLayerInteractor.requestHikingLayerFileFlow()

            flow.test {
                assertThat(awaitItem()).isEqualTo(expectedFile)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestHikingLayerFileFlow, then unknown domain exception is emitted`() {
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.getHikingLayerFile() } throws exception

            val flow = hikingLayerInteractor.requestHikingLayerFileFlow()

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    @Test
    fun `Given hiking layer file ID, when requestDownloadHikingLayerFileFlow, then download id is emitted`() {
        runTest {
            val downloadId = 12345L
            coEvery { hikingLayerRepository.downloadHikingLayerFile() } returns downloadId

            val flow = hikingLayerInteractor.requestDownloadHikingLayerFileFlow()

            flow.test {
                assertThat(awaitItem()).isEqualTo(downloadId)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestDownloadHikingLayerFileFlow, then unknown domain exception is emitted`() {
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.downloadHikingLayerFile() } throws exception

            val flow = hikingLayerInteractor.requestDownloadHikingLayerFileFlow()

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    @Test
    fun `Given hiking layer file ID, when requestSaveHikingLayerFile, then success task result returns`() {
        runTest {
            val downloadId = 12345L
            coEvery { hikingLayerRepository.saveHikingLayerFile(downloadId) } returns Unit

            val flow = hikingLayerInteractor.requestSaveHikingLayerFileFlow(downloadId)

            flow.test {
                assertThat(awaitItem()).isEqualTo(Unit)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestSaveHikingLayerFile, then error task result returns with mapped exception`() {
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.saveHikingLayerFile(any()) } throws exception

            val flow = hikingLayerInteractor.requestSaveHikingLayerFileFlow(12345L)

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

}

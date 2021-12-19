package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

@ExperimentalCoroutinesApi
class HikingLayerInteractorTest {

    private lateinit var hikingLayerInteractor: HikingLayerInteractor

    private val hikingLayerRepository = mockk<HikingLayerRepository>()

    @Before
    fun setUp() {
        hikingLayerInteractor = HikingLayerInteractor(TestTaskExecutor(), hikingLayerRepository)
    }

    @Test
    fun `Given file, when requestGetHikingLayer, then success task result returns`() {
        runBlockingTest {
            val expectedFile = File("pathName")
            coEvery { hikingLayerRepository.getHikingLayerFile() } returns expectedFile

            val taskResult = hikingLayerInteractor.requestGetHikingLayerFile()

            assertThat(taskResult).isEqualTo(TaskResult.Success(expectedFile))
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetHikingLayer, then error task result returns with mapped exception`() {
        runBlockingTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.getHikingLayerFile() } throws exception

            val taskResult = hikingLayerInteractor.requestGetHikingLayerFile()

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(GeneralDomainExceptionMapper.map(exception))
            )
        }
    }

    @Test
    fun `Given hiking layer file ID, when requestDownloadHikingLayer, then success task result returns`() {
        runBlockingTest {
            val expectedId = 12345L
            coEvery { hikingLayerRepository.downloadHikingLayerFile() } returns expectedId

            val taskResult = hikingLayerInteractor.requestDownloadHikingLayerFile()

            assertThat(taskResult).isEqualTo(TaskResult.Success(expectedId))
        }
    }

    @Test
    fun `Given IllegalStateException, when downloadHikingLayerFile, then error task result returns with mapped exception`() {
        runBlockingTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.downloadHikingLayerFile() } throws exception

            val taskResult = hikingLayerInteractor.requestDownloadHikingLayerFile()

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(GeneralDomainExceptionMapper.map(exception))
            )
        }
    }

    @Test
    fun `Given hiking layer file ID, when requestSaveHikingLayerFile, then success task result returns`() {
        runBlockingTest {
            val expectedId = 12345L
            coEvery { hikingLayerRepository.saveHikingLayerFile(expectedId) } returns Unit

            val taskResult = hikingLayerInteractor.requestSaveHikingLayerFile(expectedId)

            assertThat(taskResult).isEqualTo(TaskResult.Success(Unit))
        }
    }

    @Test
    fun `Given IllegalStateException, when requestSaveHikingLayerFile, then error task result returns with mapped exception`() {
        runBlockingTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { hikingLayerRepository.saveHikingLayerFile(any()) } throws exception

            val taskResult = hikingLayerInteractor.requestSaveHikingLayerFile(12345L)

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(GeneralDomainExceptionMapper.map(exception))
            )
        }
    }

    @Test
    fun `Given FileNotFoundException, when requestSaveHikingLayerFile, then error task result returns with domain exception which contains message`() {
        runBlockingTest {
            val exception = FileNotFoundException()
            coEvery { hikingLayerRepository.saveHikingLayerFile(any()) } throws exception

            val taskResult = hikingLayerInteractor.requestSaveHikingLayerFile(12345L)

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(
                    DomainException(
                        throwable = exception,
                        messageRes = R.string.download_layer_missing_downloaded_file.toMessage()
                    )
                )
            )
        }
    }

}

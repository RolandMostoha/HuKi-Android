package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
        hikingLayerInteractor = HikingLayerInteractor(TestTaskExecutor(), exceptionLogger, hikingLayerRepository)

        every { exceptionLogger.recordException(any()) } returns Unit
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
                TaskResult.Error(DomainExceptionMapper.map(exception))
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
                TaskResult.Error(DomainExceptionMapper.map(exception))
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
                TaskResult.Error(DomainExceptionMapper.map(exception))
            )
        }
    }

}

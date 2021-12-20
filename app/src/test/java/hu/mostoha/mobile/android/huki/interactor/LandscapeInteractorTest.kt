package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_OSM_ID
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LandscapeInteractorTest {

    private lateinit var landscapeInteractor: LandscapeInteractor

    private val landscapeRepository = mockk<LandscapeRepository>()
    private val exceptionLogger = mockk<ExceptionLogger>()

    @Before
    fun setUp() {
        landscapeInteractor = LandscapeInteractor(TestTaskExecutor(), exceptionLogger, landscapeRepository)

        every { exceptionLogger.recordException(any()) } returns Unit
    }

    @Test
    fun `Given landscapes list, when requestGetLandscapes, then success task result returns`() {
        runBlockingTest {
            val landscapes = listOf(DEFAULT_LANDSCAPE)
            coEvery { landscapeRepository.getLandscapes() } returns landscapes

            val taskResult = landscapeInteractor.requestGetLandscapes()

            assertThat(taskResult).isEqualTo(TaskResult.Success(landscapes))
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetLandscapes, then error task result returns with mapped exception`() {
        runBlockingTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { landscapeRepository.getLandscapes() } throws exception

            val taskResult = landscapeInteractor.requestGetLandscapes()

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(DomainExceptionMapper.map(exception))
            )
        }
    }

    companion object {
        private val DEFAULT_LANDSCAPE = Landscape(
            osmId = DEFAULT_LANDSCAPE_OSM_ID,
            name = DEFAULT_LANDSCAPE_NAME,
            type = LandscapeType.MOUNTAIN_RANGE_LOW,
            center = Location(DEFAULT_LANDSCAPE_LATITUDE, DEFAULT_LANDSCAPE_LONGITUDE)
        )
    }

}

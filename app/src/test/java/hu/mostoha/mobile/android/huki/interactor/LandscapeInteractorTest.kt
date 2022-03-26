package hu.mostoha.mobile.android.huki.interactor

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
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
        landscapeInteractor = LandscapeInteractor(exceptionLogger, landscapeRepository)

        every { exceptionLogger.recordException(any()) } returns Unit
    }

    @Test
    fun `Given landscapes list, when requestGetLandscapes, then landscapes are emitted`() {
        runBlockingTest {
            val landscapes = listOf(DEFAULT_LANDSCAPE)
            coEvery { landscapeRepository.getLandscapes() } returns landscapes

            val flow = landscapeInteractor.requestGetLandscapesFlow()

            flow.test {
                assertThat(awaitItem()).isEqualTo(landscapes)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetLandscapes, then unknown domain exception is emitted`() {
        runBlockingTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { landscapeRepository.getLandscapes() } throws exception

            val flow = landscapeInteractor.requestGetLandscapesFlow()

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
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

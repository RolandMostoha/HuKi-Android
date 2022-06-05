package hu.mostoha.mobile.android.huki.interactor

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.testdata.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
        runTest {
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
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { landscapeRepository.getLandscapes() } throws exception

            val flow = landscapeInteractor.requestGetLandscapesFlow()

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    @Test
    fun `Given landscapes list, when requestGetLandscapes by location, then landscapes are emitted in order by distance`() {
        runTest {
            val landscapes = listOf(DEFAULT_LANDSCAPE_2, DEFAULT_LANDSCAPE)
            coEvery { landscapeRepository.getLandscapes() } returns landscapes

            val flow = landscapeInteractor.requestGetLandscapesFlow(DEFAULT_LANDSCAPE.center)

            flow.test {
                assertThat(awaitItem()).isEqualTo(listOf(DEFAULT_LANDSCAPE, DEFAULT_LANDSCAPE_2))
                awaitComplete()
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
        private val DEFAULT_LANDSCAPE_2 = Landscape(
            osmId = DEFAULT_LANDSCAPE_2_OSM_ID,
            name = DEFAULT_LANDSCAPE_2_NAME,
            type = LandscapeType.MOUNTAIN_RANGE_LOW,
            center = Location(DEFAULT_LANDSCAPE_2_LATITUDE, DEFAULT_LANDSCAPE_2_LONGITUDE)
        )
    }

}

package hu.mostoha.mobile.android.huki.interactor

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_CLOSED
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LayersInteractorTest {

    private lateinit var layersInteractor: LayersInteractor

    private val layersRepository = mockk<LayersRepository>()

    private val exceptionLogger = mockk<ExceptionLogger>()

    private val gpxFileUri = mockk<Uri>()

    @Before
    fun setUp() {
        layersInteractor = LayersInteractor(exceptionLogger, layersRepository)

        every { exceptionLogger.recordException(any()) } returns Unit
    }

    @Test
    fun `Given tile zoom range list, when requestHikingLayerZoomRanges, then zoom ranges are emitted`() {
        runTest {
            val zoomRanges = listOf(TileZoomRange(10, 50, 100, 30, 70))
            coEvery { layersRepository.getHikingLayerZoomRanges() } returns zoomRanges

            val flow = layersInteractor.requestHikingLayerZoomRanges()

            flow.test {
                assertThat(awaitItem()).isEqualTo(zoomRanges)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestHikingLayerZoomRanges, then unknown domain exception is emitted`() {
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { layersRepository.getHikingLayerZoomRanges() } throws exception

            val flow = layersInteractor.requestHikingLayerZoomRanges()

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    @Test
    fun `Given file uri, when requestGpx, then gpx is emitted`() {
        runTest {
            val gpxDetails = DEFAULT_GPX_DETAILS
            coEvery { layersRepository.getGpxDetails(gpxFileUri) } returns gpxDetails

            val flow = layersInteractor.requestGpxDetails(gpxFileUri)

            flow.test {
                assertThat(awaitItem()).isEqualTo(gpxDetails)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGpx, then unknown domain exception is emitted`() {
        runTest {
            val exception = IllegalStateException("Unknown exception")
            coEvery { layersRepository.getGpxDetails(gpxFileUri) } throws exception

            val flow = layersInteractor.requestGpxDetails(gpxFileUri)

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    companion object {
        private val DEFAULT_GPX_DETAILS = GpxDetails(
            fileName = "dera_szurdok.gpx",
            locations = DEFAULT_GPX_WAY_CLOSED.map { Location(it.first, it.second) },
            gpxWaypoints = emptyList(),
            travelTime = DEFAULT_GPX_WAY_CLOSED
                .map { Location(it.first, it.second) }
                .calculateTravelTime(),
            distance = 15000,
            altitudeRange = 300 to 800,
            incline = 500,
            decline = 300,
        )
    }

}

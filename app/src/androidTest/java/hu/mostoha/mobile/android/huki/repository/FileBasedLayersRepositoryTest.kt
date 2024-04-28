package hu.mostoha.mobile.android.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.interactor.exception.GpxUriNullException
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class FileBasedLayersRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: FileBasedLayersRepository

    @Inject
    lateinit var gpxConfiguration: GpxConfiguration

    @Test
    fun givenTileZoomRangesRawResource_whenGetHikingLayerZoomRanges_thenCorrectTileZoomRangeListReturns() =
        runTest {
            val zoomRanges = repository.getHikingLayerZoomRanges()

            assertThat(zoomRanges).isNotEmpty()
        }

    @Test
    fun givenGpxFileUri_whenGetGpx_thenCorrectGpxReturns() =
        runTest {
            val uri = getTestGpxFileUri()

            val gpx = repository.getGpxDetails(uri)

            assertThat(gpx).isNotNull()
        }

    @Test
    fun givenNullGpxFileUri_whenGetGpx_thenGpxParsedExceptionThrows() {
        val uri = null

        assertThrows(GpxUriNullException::class.java) {
            runTest {
                repository.getGpxDetails(uri)
            }
        }
    }

    @Test
    fun givenGpxFiles_whenGetGpxHistory_thenGpxHistoryReturnsWithoutMalformed() =
        runTest {
            saveTestGpxHistoryFiles()

            val gpx = repository.getGpxHistory()

            assertThat(gpx.externalGpxList).hasSize(1)
        }

    private fun getTestGpxFileUri(): Uri {
        val inputStream = testContext.assets.open("dera_szurdok.gpx")
        val file = File(testAppContext.cacheDir.path + "/dera_szurdok.gpx").apply {
            copyFrom(inputStream)
        }

        return Uri.fromFile(file)
    }

    private fun saveTestGpxHistoryFiles() {
        val inputStream1 = testContext.assets.open("dera_szurdok.gpx")
        File("${gpxConfiguration.getExternalGpxDirectory()}/dera_szurdok.gpx").apply {
            copyFrom(inputStream1)
        }
        val inputStream2 = testContext.assets.open("malformed_gpx.gpx")
        File("${gpxConfiguration.getExternalGpxDirectory()}/malformed_gpx.gpx").apply {
            copyFrom(inputStream2)
        }
    }

}

package hu.mostoha.mobile.android.huki.repository

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
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

        assertThrows(GpxParseFailedException::class.java) {
            runTest {
                repository.getGpxDetails(uri)
            }
        }
    }

    private fun getTestGpxFileUri(): Uri {
        val inputStream = testContext.assets.open("dera_szurdok.gpx")
        val file = File(testAppContext.cacheDir.path + "/dera_szurdok.gpx").apply {
            copyFrom(inputStream)
        }

        return Uri.fromFile(file)
    }

}
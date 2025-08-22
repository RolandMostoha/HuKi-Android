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
import hu.mostoha.mobile.android.huki.util.sleepFor
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class DefaultGpxRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: DefaultGpxRepository

    @Inject
    lateinit var gpxConfiguration: GpxConfiguration

    @Test
    fun givenGpxFileUri_whenGetGpx_thenCorrectGpxReturns() =
        runTest {
            val uri = saveTestGpxInCache()

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

    @Test
    fun givenNoGpxFiles_whenGetRecentGpxHistory_thenReturnsEmptyList() = runTest {
        val recentHistory = repository.getRecentGpxHistory()

        assertThat(recentHistory).isEmpty()
    }

    @Test
    fun givenOpenedGpxFiles_whenGetRecentGpxHistory_thenGpxIsPresentInRecentHistory() = runTest {
        val openedGpx = repository.getGpxDetails(saveTestGpxInCache())

        val recentHistory = repository.getRecentGpxHistory()

        assertTrue(recentHistory.any { it.fileUri.toString() == openedGpx.fileUri })
    }

    @Test
    fun givenOpenedAndSavedGpxFiles_whenGetRecentGpxHistory_then4MostRecentHistoryReturns() = runTest {
        repeat(5) { index ->
            saveTestGpxHistoryFile("saved$index")
            // File system lastModified doesn't record millis
            sleepFor(1001)
        }

        repository.getGpxDetails(saveTestGpxInCache("recent1"))
        repository.getGpxDetails(saveTestGpxInCache("recent2"))

        val recentHistory = repository.getRecentGpxHistory()

        assertThat(recentHistory).hasSize(4)
        assertTrue(recentHistory[0].fileUri.toString().contains("recent2"))
        assertTrue(recentHistory[1].fileUri.toString().contains("recent1"))
        assertTrue(recentHistory[2].fileUri.toString().contains("saved4"))
        assertTrue(recentHistory[3].fileUri.toString().contains("saved3"))
    }

    @Test
    fun givenOpenedGpxFile_whenRenameGpx_thenItIsDeletedFromDatabaseAndNameIsUpdated() = runTest {
        val fileUri = saveTestGpxHistoryFile("recent")
        repository.getGpxDetails(fileUri)
        repository.renameGpx(fileUri, "recentModified")

        val recentHistory = repository.getRecentGpxHistory()

        assertTrue(recentHistory[0].fileUri.toString().contains("recentModified"))
    }

    @Test
    fun givenOpenedGpxFile_whenDeleteGpx_thenItIsDeletedFromDatabase() = runTest {
        val fileUri = saveTestGpxHistoryFile("recent")
        repository.getGpxDetails(fileUri)
        repository.deleteGpx(fileUri)

        val recentHistory = repository.getRecentGpxHistory()

        assertThat(recentHistory).isEmpty()
    }

    private fun saveTestGpxInCache(filePostFix: String = ""): Uri {
        val inputStream = testContext.assets.open("dera_szurdok.gpx")
        val file = File(testAppContext.cacheDir.path + "/dera_szurdok${filePostFix}.gpx").apply {
            copyFrom(inputStream)
        }
        return Uri.fromFile(file)
    }

    private fun saveTestGpxHistoryFile(filePostFix: String = ""): Uri {
        val inputStream = testContext.assets.open("dera_szurdok.gpx")
        val file = File("${gpxConfiguration.getExternalGpxDirectory()}/dera_szurdok${filePostFix}.gpx").apply {
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

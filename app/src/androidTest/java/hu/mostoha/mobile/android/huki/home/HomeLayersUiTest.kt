package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.ServiceModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.ui.home.OverlayPositions
import hu.mostoha.mobile.android.huki.util.espresso.clickInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlayInPosition
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class HomeLayersUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @BindValue
    @JvmField
    val hikingLayerRepository: HikingLayerRepository = mockk()

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun givenNullLayerFile_whenScreenStarts_thenLayersPopupWindowShouldShown() {
        answerNullHikingLayer()

        launchScenario<HomeActivity> {
            R.string.layers_base_layers_title.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenNullLayerFile_whenLayerIsDownloaded_thenLayersPopupWindowShouldUpdate() {
        coEvery {
            hikingLayerRepository.getHikingLayerFile()
        } returnsMany listOf(
            null,
            osmConfiguration.getHikingLayerFile().apply {
                copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
            }
        )
        coEvery { hikingLayerRepository.downloadHikingLayerFile() } returns 1
        coEvery { hikingLayerRepository.saveHikingLayerFile(1) } returns Unit

        launchScenario<HomeActivity> {
            R.id.itemLayersHikingDownloadButton.clickInPopup()

            testAppContext.sendBroadcast(Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE).apply {
                putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 1L)
            })

            R.string.layers_hiking_downloaded_label.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenTuraReteg1000_whenMapOpens_thenHikingLayerDisplays() {
        answerTestHikingLayer()

        launchScenario<HomeActivity> {
            R.id.homeMapView.hasOverlayInPosition<TilesOverlay>(OverlayPositions.HIKING_LAYER)
        }
    }

    private fun answerTestHikingLayer() {
        val file = osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns file
    }

    private fun answerNullHikingLayer() {
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns null
    }

}

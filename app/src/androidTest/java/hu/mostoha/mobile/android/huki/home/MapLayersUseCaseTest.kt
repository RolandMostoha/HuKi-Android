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
import hu.mostoha.mobile.android.huki.util.espresso.clickInPopup
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.launch
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class MapLayersUseCaseTest {

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
    val layerRepository: HikingLayerRepository = mockk()

    @BindValue
    @JvmField
    val placeRepository: PlacesRepository = mockk()

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

        launch<HomeActivity> {
            R.string.layers_base_layers_title.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenNullLayerFile_whenLayerIsDownloaded_thenLayersPopupWindowShouldUpdate() {
        coEvery {
            layerRepository.getHikingLayerFile()
        } returnsMany listOf(
            null,
            osmConfiguration.getHikingLayerFile().apply {
                copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
            }
        )
        coEvery { layerRepository.downloadHikingLayerFile() } returns 1
        coEvery { layerRepository.saveHikingLayerFile(1) } returns Unit

        launch<HomeActivity> {
            R.id.itemLayersHikingDownloadButton.clickInPopup()

            testAppContext.sendBroadcast(Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE).apply {
                putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 1L)
            })

            R.string.layers_hiking_update_label.isPopupTextDisplayed()
        }
    }

    private fun answerNullHikingLayer() {
        coEvery { layerRepository.getHikingLayerFile() } returns null
    }

    private fun answerTestHikingLayer() {
        coEvery { layerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

}
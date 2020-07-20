package hu.mostoha.mobile.android.turistautak.home

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.di.module.RepositoryModule
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.extensions.copyFrom
import hu.mostoha.mobile.android.turistautak.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.repository.LayerRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.checkTextDisplayed
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.checkViewDisplayed
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.clickViewWithId
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.clickViewWithText
import hu.mostoha.mobile.android.turistautak.util.launch
import hu.mostoha.mobile.android.turistautak.util.testContext
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
class HomeActivityTest {

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
    val layerRepository: LayerRepository = mockk()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun givenNullLayerFile_whenHomeOpens_thenDownloadDialogDisplays() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            checkTextDisplayed(R.string.download_layer_dialog_title)
        }
    }

    @Test
    fun givenNullLayerFile_whenDialogCanceled_thenMapShouldShown() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            clickViewWithText(R.string.download_layer_dialog_negative_button)

            checkViewDisplayed(R.id.homeMapView)
        }
    }

    @Test
    fun givenNullLayerFile_whenMyLocationClicked_thenMyLocationOverlayDisplays() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            clickViewWithText(R.string.download_layer_dialog_negative_button)
            clickViewWithId(R.id.homeMyLocationButton)

            // TODO: Check overlay displays
        }
    }

    @Test
    fun givenTuraReteg1000_whenHomeOpens_thenLayerDisplays() {
        val fileName = "TuraReteg_1000.mbtiles"
        val file = osmConfiguration.getHikingLayerFile().also {
            it.copyFrom(testContext.assets.open(fileName))
        }
        coEvery { layerRepository.getHikingLayerFile() } returns file

        launch<HomeActivity> {
            // TODO: Check overlay displays
        }
    }

}


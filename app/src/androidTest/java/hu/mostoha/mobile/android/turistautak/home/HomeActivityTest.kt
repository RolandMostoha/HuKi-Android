package hu.mostoha.mobile.android.turistautak.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.di.module.RepositoryModule
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.repository.LayerRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.checkTextDisplayed
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.checkViewDisplayed
import hu.mostoha.mobile.android.turistautak.util.launch
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class HomeActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val layerRepository: LayerRepository = mockk()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun whenScreenStarts_thenMapShouldShown() {
        launch<HomeActivity> {
            checkViewDisplayed(R.id.homeMapView)
        }
    }

    @Test
    fun givenMissingHikingLayerFile_whenHomeOpens_thenDownloadDialogDisplays() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            checkTextDisplayed(R.string.download_layer_dialog_title)
        }
    }

}


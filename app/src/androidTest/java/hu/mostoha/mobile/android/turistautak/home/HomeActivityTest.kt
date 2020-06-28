package hu.mostoha.mobile.android.turistautak.home

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.di.module.RepositoryModule
import hu.mostoha.mobile.android.turistautak.repository.LayerRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.EspressoAssertions.checkDisplayed
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
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
        launchHome {
            onView(withId(R.id.homeMapView)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun givenMissingHikingLayerFile_whenHomeOpens_thenDownloadDialogDisplays() {
        every { layerRepository.getHikingLayerFile() } returns null

        launchHome {
            checkDisplayed(R.string.download_layer_dialog_title)
        }
    }

    private fun launchHome(then: () -> Unit) {
        val scenario = launchActivity<HomeActivity>()

        then.invoke()

        scenario.close()
    }

}


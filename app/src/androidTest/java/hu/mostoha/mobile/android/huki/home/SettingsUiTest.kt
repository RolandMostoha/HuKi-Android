package hu.mostoha.mobile.android.huki.home

import android.content.Intent
import android.net.MailTo
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.hasSliderValue
import hu.mostoha.mobile.android.huki.util.espresso.hasTileScaleFactor
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.setSliderValue
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toPercentageFromScale
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(
    VersionConfigurationModule::class
)
class SettingsUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @BindValue
    @JvmField
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()
    }

    @Test
    fun whenClickOnContactFab_thenContactDialogDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeSettingsFab.click()

            R.id.settingsContainer.isDisplayed()
            R.string.settings_map_scale_title.isTextDisplayed()
            R.string.settings_theme_title.isTextDisplayed()
            R.string.settings_offline_mode_title.isTextDisplayed()
            R.string.settings_contact_title.isTextDisplayed()
        }
    }

    @Test
    fun whenSetSliderValue_thenMapScaleFactorUpdates() {
        launchScenario<HomeActivity> {
            R.id.homeMapView.hasTileScaleFactor(MAP_DEFAULT_SCALE_FACTOR)

            R.id.homeSettingsFab.click()

            R.id.settingsMapScaleSlider.hasSliderValue(MAP_DEFAULT_SCALE_FACTOR.toPercentageFromScale().toFloat())

            R.id.settingsMapScaleSlider.setSliderValue(100f)

            pressBack()

            R.id.homeMapView.hasTileScaleFactor(1.0)

            R.id.homeSettingsFab.click()

            R.id.settingsMapScaleSlider.hasSliderValue(100f)
        }
    }

    @Test
    fun whenClickOnEmail_thenEmailIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeSettingsFab.click()
            R.id.settingsEmailText.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_SENDTO),
                    hasData(MailTo.MAILTO_SCHEME),
                    hasExtra(Intent.EXTRA_EMAIL, arrayOf(testAppContext.getString(R.string.settings_email))),
                    hasExtra(Intent.EXTRA_SUBJECT, testAppContext.getString(R.string.settings_email_subject))
                )
            )
        }
    }

    @Test
    fun whenClickOnGitHubRepositoryText_thenBrowserIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeSettingsFab.click()
            R.id.settingsGitHubText.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(testAppContext.getString(R.string.settings_github_repository_url))
                )
            )
        }
    }

}

package hu.mostoha.mobile.android.huki.home

import android.content.Intent
import android.net.MailTo
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithScroll
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class SupportUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()
    }

    @Test
    fun whenClickOnSupportFab_thenSupportScreenDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeSupportFab.click()

            R.id.supportContainer.isDisplayed()
        }
    }

    @Test
    fun whenClickOnEmail_thenEmailIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeSupportFab.click()
            R.id.supportContactEmail.clickWithScroll()

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

}

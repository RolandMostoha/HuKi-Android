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
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class HomeContactUiTest {

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
    fun whenClickOnContactFab_thenContactDialogDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeContactFab.click()

            R.string.contact_subtitle.isTextDisplayed()
        }
    }

    @Test
    fun whenClickOnEmail_thenEmailIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeContactFab.click()
            R.id.contactEmail.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_SENDTO),
                    hasData(MailTo.MAILTO_SCHEME),
                    hasExtra(Intent.EXTRA_EMAIL, arrayOf(testAppContext.getString(R.string.contact_email))),
                    hasExtra(Intent.EXTRA_SUBJECT, testAppContext.getString(R.string.contact_email_subject))
                )
            )
        }
    }

    @Test
    fun whenClickOnGitHubRepositoryText_thenBrowserIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeContactFab.click()
            R.id.contactGitHub.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(testAppContext.getString(R.string.contact_github_repository_url))
                )
            )
        }
    }

}

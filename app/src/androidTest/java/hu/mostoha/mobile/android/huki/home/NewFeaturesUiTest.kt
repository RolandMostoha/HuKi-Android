package hu.mostoha.mobile.android.huki.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.doesNotExist
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class NewFeaturesUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun whenNewFeaturesWasNotSeen_thenNewFeaturesDialogIsShown() {
        launchScenario<HomeActivity> {
            testAppContext.getString(R.string.new_features_title_template)
                .format(BuildConfig.VERSION_NAME)
                .isTextDisplayed()
        }
    }

    @Test
    fun whenNewFeaturesOkButtonIsClicked_thenNewFeaturesDialogIsShownAnymore() {
        launchScenario<HomeActivity> {
            testAppContext.getString(R.string.new_features_title_template)
                .format(BuildConfig.VERSION_NAME)
                .isTextDisplayed()

            R.id.newFeaturesOkButton.click()

            recreate()

            R.id.newFeaturesOkButton.doesNotExist()
        }
    }

    @Test
    fun whenNewFeaturesCloseButtonIsClicked_thenNewFeaturesDialogIsShownAnymore() {
        launchScenario<HomeActivity> {
            testAppContext.getString(R.string.new_features_title_template)
                .format(BuildConfig.VERSION_NAME)
                .isTextDisplayed()

            R.id.newFeaturesCloseButton.click()

            recreate()

            R.id.newFeaturesCloseButton.doesNotExist()
        }
    }

}

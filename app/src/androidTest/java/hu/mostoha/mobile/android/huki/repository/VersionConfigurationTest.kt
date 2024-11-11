package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class VersionConfigurationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var configuration: VersionDataStoreConfiguration

    @Test
    fun givenLatestVersion_whenGetNewFeatures_thenWhatsNewIsReturned() {
        runTest {
            Locale.setDefault(Locale.US)

            val newFeatures = configuration.getNewFeatures(BuildConfig.VERSION_NAME).first()

            assertThat(newFeatures).isNotEmpty()
        }
    }

    @Test
    fun givenUKLocale_whenGetNewFeatures_thenUSWhatsNewIsReturned() {
        runTest {
            Locale.setDefault(Locale.UK)

            val newFeatures = configuration.getNewFeatures(BuildConfig.VERSION_NAME).first()

            assertThat(newFeatures).isNotEmpty()
        }
    }

    @Test
    fun givenOldVersion_whenGetNewFeatures_thenWhatsNewIsReturned() {
        runTest {
            Locale.setDefault(Locale.forLanguageTag("hu-HU"))

            val newFeatures = configuration.getNewFeatures("v1.0.4").first()

            assertThat(newFeatures).contains("- GPX beolvasási hibák javítása")
        }
    }

    @Test
    fun givenNonExistingVersion_whenGetNewFeatures_thenWhatsNewIsReturned() {
        runTest {
            val newFeatures = configuration.getNewFeatures("v9.9.9").first()

            assertThat(newFeatures).isNull()
        }
    }

    @Test
    fun whenSaveNewFeaturesSeen_thenWhatsNewIsReturned() {
        runTest {
            configuration.saveNewFeaturesSeen("v1.0.4")

            val newFeatures = configuration.getNewFeatures("v1.0.4").first()

            assertThat(newFeatures).isNull()
        }
    }

    @Test
    fun givenOldVersion_whenSaveNewFeaturesSeen_thenLatestWhatsNewIsReturned() {
        runTest {
            Locale.setDefault(Locale.US)

            configuration.saveNewFeaturesSeen("v1.0.4")
            val newFeatures = configuration.getNewFeatures("v1.0.5").first()

            assertThat(newFeatures).contains("- Update OKT routes and stamp locations")
        }
    }

}

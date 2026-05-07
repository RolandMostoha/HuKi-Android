package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolygon
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolyline
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.waitForBottomSheetState
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(
    RepositoryModule::class,
    LocationModule::class,
    VersionConfigurationModule::class
)
class DiscoverUiTest {

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
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

    @BindValue
    @JvmField
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val geocodingRepository: GeocodingRepository = mockk()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()
    }

    @Test
    fun whenClickOnLandscapes_thenLandscapeMapDisplays() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeMapView.hasNoOverlay<LandscapePolygon>()

            R.id.homeDiscoverFab.click()
            R.string.discover_landscapes_button_title.clickWithText()

            R.id.homeMapView.hasOverlay<LandscapePolygon>()
        }
    }

    @Test
    fun whenClickOnLandscapesInfo_thenDestinationsInfoDisplays() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeDiscoverFab.click()
            R.id.discoverLandscapesInfo.click()

            R.string.destinations_info.isPopupTextDisplayed()
        }
    }

    @Test
    fun whenClickOnLandscape_thenLandscapeOutlineDisplays() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeMapView.hasNoOverlay<LandscapePolyline>()

            R.id.homeDiscoverFab.click()
            R.string.discover_landscapes_button_title.clickWithText()

            waitForBottomSheetState()

            R.string.landscape_aggteleki_karszt.clickWithText()

            R.id.homeMapView.hasOverlay<LandscapePolyline>()
        }
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

}

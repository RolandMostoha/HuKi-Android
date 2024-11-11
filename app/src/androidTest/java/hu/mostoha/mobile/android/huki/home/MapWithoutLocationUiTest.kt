package hu.mostoha.mobile.android.huki.home

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
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launchScenario
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
class MapWithoutLocationUiTest {

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
    }

    @Test
    fun givenMapsWithoutLocation_whenScreenOpens_thenMyLocationOverlayDoesNotDisplay() {
        launchScenario<HomeActivity> {
            R.id.homeMapView.hasNoOverlay<MyLocationOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenNoLocationPermission_whenClickInPlaceFinder_thenStaticActionsDisplay() {
        launchScenario<HomeActivity> {
            val searchText = " "

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.place_finder_my_location_button.isPopupTextDisplayed()
            R.string.place_finder_pick_location_button.isPopupTextDisplayed()
        }
    }

}

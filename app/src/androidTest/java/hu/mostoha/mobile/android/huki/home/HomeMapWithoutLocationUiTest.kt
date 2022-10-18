package hu.mostoha.mobile.android.huki.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.launchScenario
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class HomeMapWithoutLocationUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

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

}

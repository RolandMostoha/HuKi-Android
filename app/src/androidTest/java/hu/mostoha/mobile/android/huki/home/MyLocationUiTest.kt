package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayedContainsText
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayedWithText
import hu.mostoha.mobile.android.huki.util.espresso.isFollowLocationEnabled
import hu.mostoha.mobile.android.huki.util.espresso.swipeDown
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(LocationModule::class)
class MyLocationUiTest {

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
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun whenLocationPermissionEnabled_thenMyLocationOverlayDisplaysAndFollowLocationIsEnabled() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeMapView.hasOverlay<MyLocationOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
            R.id.homeMapView.isFollowLocationEnabled(true)
        }
    }

    @Test
    fun whenMapViewIsScrolled_thenFollowLocationIsDisabled() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeMapView.click()

            R.id.homeMapView.isFollowLocationEnabled(false)
        }
    }

    @Test
    fun whenMyLocationClicked_thenFollowLocationIsEnabled() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeMapView.swipeDown()
            R.id.homeMyLocationFab.click()

            R.id.homeMapView.isFollowLocationEnabled(true)
        }
    }

    @Test
    fun whenAltitudeIsAvailable_thenAltitudeTextDisplays() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeAltitudeText.isDisplayedContainsText("162 m")
        }
    }

    @Test
    fun whenAltitudeIsNotAvailable_thenAltitudeTextDoesNotDisplay() {
        val location = DEFAULT_MY_LOCATION
            .copy(altitude = null)
            .toMockLocation()
        coEvery { asyncMyLocationProvider.getLocationFlow() } returns flowOf(location)

        launchScenario<HomeActivity> {
            R.id.homeAltitudeText.isDisplayedWithText("")
        }
    }

    @Test
    fun whenRecreate_thenMyLocationOverlayDisplaysAgain() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> { scenario ->
            R.id.homeMapView.hasOverlay<MyLocationOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            scenario.recreate()

            R.id.homeMapView.hasOverlay<MyLocationOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.startLocationProvider(any()) } returns true
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns DEFAULT_MY_LOCATION.toMockLocation()
    }

    companion object {
        private val DEFAULT_MY_LOCATION = Location(
            DEFAULT_MY_LOCATION_LATITUDE,
            DEFAULT_MY_LOCATION_LONGITUDE,
            DEFAULT_MY_LOCATION_ALTITUDE
        )
    }

}

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
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isFollowLocationEnabled
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
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
@UninstallModules(
    LocationModule::class,
    VersionConfigurationModule::class
)
class HikeModeUiTest {

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

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()

    }

    @Test
    fun whenToggleHikeModeFab_thenHeaderAndFabVisibilityChanges() {
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            R.id.homeHikeModeFab.click()

            R.id.homeRoutePlannerFab.isNotDisplayed()
            R.id.homeCompassFab.isDisplayed()
            R.id.homeHistoryFab.isNotDisplayed()
            R.id.homePlaceCategoriesFab.isNotDisplayed()
            R.id.homeSearchBarContainer.isNotDisplayed()
            R.id.mapZoomInFab.isDisplayed()
            R.id.mapZoomOutFab.isDisplayed()
            R.id.homeMapView.isFollowLocationEnabled(true)

            R.id.homeHikeModeFab.click()

            R.id.homeRoutePlannerFab.isDisplayed()
            R.id.homeCompassFab.isNotDisplayed()
            R.id.homeHistoryFab.isDisplayed()
            R.id.homePlaceCategoriesFab.isDisplayed()
            R.id.homeSearchBarContainer.isDisplayed()
            R.id.mapZoomInFab.isNotDisplayed()
            R.id.mapZoomOutFab.isNotDisplayed()
            R.id.homeMapView.isFollowLocationEnabled(false)
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

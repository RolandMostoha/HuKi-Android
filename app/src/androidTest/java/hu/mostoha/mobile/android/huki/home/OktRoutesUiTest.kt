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
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktBasePolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithContentDescription
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.waitFor
import hu.mostoha.mobile.android.huki.util.espresso.waitForRecreate
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Ignore
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
class OktRoutesUiTest {

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
    val layersRepository: LayersRepository = FileBasedLayersRepository(
        testAppContext,
        UnconfinedTestDispatcher(),
        LayersDomainModelMapper(),
        HukiGpxConfiguration(testAppContext),
        FakeExceptionLogger(),
    )

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val geocodingRepository: GeocodingRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()

        coEvery { geocodingRepository.getPlaceProfile(any()) } returns null
    }

    @Test
    fun whenClickOnOktChip_thenOktBottomSheetDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            R.string.okt_okt_subtitle.isTextDisplayed()
        }
    }

    @Test
    fun whenClickOnRpddkChip_thenRpddkBottomSheetDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "RPDDK".clickWithTextInPopup()

            waitForMapClear()

            R.string.okt_rpddk_subtitle.isTextDisplayed()
        }
    }

    @Test
    fun whenClickOnOktChip_thenBaseRouteDisplaysOnMap() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            R.id.homeMapView.hasOverlay<OktBasePolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenOktRoute_whenSelect_thenOktRouteDisplaysOnMap() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            "Írott-kő - Sárvár".clickWithText()

            waitForMapClear()

            R.id.homeMapView.hasOverlay<OktBasePolyline>()
            R.id.homeMapView.hasOverlay<OktPolyline>()
            R.id.homeMapView.hasOverlay<OktMarker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    @Ignore
    fun givenOktRoute_whenClickOnLink_thenOktWebPageRequested() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            "Írott-kő - Sárvár".clickWithText()

            waitForMapClear()

            testAppContext.getString(R.string.accessibility_okt_routes_action_button)
                .format("OKT-01")
                .clickWithContentDescription()

            R.string.okt_routes_menu_action_details.clickWithTextInPopup()
        }
    }

    @Test
    fun givenOktRoute_whenClickOnStartPoint_thenPlaceDetailsDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            "Írott-kő - Sárvár".clickWithText()

            waitForMapClear()

            testAppContext.getString(R.string.accessibility_okt_routes_action_button)
                .format("OKT-01")
                .clickWithContentDescription()

            R.string.okt_routes_menu_action_start_point.clickWithTextInPopup()

            R.id.placeDetailsContentContainer.isDisplayed()
        }
    }

    @Test
    fun givenOktRoute_whenClickOnEndPoint_thenPlaceDetailsDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            "Írott-kő - Sárvár".clickWithText()

            waitForMapClear()

            testAppContext.getString(R.string.accessibility_okt_routes_action_button)
                .format("OKT-01")
                .clickWithContentDescription()

            R.string.okt_routes_menu_action_start_point.clickWithTextInPopup()

            R.id.placeDetailsContentContainer.isDisplayed()
        }
    }

    @Test
    fun whenCloseClick_thenBottomSheetHides() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            waitForMapClear()

            R.id.oktRoutesCloseButton.click()

            waitForMapClear()

            R.id.homeMapView.hasNoOverlay<OktPolyline>()
            R.id.homeMapView.hasNoOverlay<OktMarker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun whenRecreate_thenOktRoutesDisplaysAgain() {
        launchScenario<HomeActivity> {
            R.id.homeOktFab.click()
            "OKT".clickWithTextInPopup()

            recreate()
            waitForRecreate()

            R.string.okt_okt_subtitle.isTextDisplayed()
            R.id.homeMapView.hasOverlay<MyLocationOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun waitForMapClear() {
        waitFor(300)
    }

}

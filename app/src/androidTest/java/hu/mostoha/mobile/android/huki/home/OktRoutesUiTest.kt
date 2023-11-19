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
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
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
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @BindValue
    @JvmField
    val layersRepository: LayersRepository = FileBasedLayersRepository(
        testAppContext,
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

        coEvery { geocodingRepository.getPlace(any(), any()) } returns null
    }

    @Test
    fun whenClickOnOktChip_thenOktBottomSheetDisplays() {
        launchScenario<HomeActivity> {
            R.string.okt_routes_chip_label.clickWithText()

            R.string.okt_routes_bottom_sheet_title.isTextDisplayed()
        }
    }

    @Test
    fun whenClickOnOktChip_thenBaseRouteDisplaysOnMap() {
        launchScenario<HomeActivity> {
            R.string.okt_routes_chip_label.clickWithText()

            waitForMapClear()

            R.id.homeMapView.hasOverlay<OktBasePolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenOktRoute_whenSelect_thenOktRouteDisplaysOnMap() {
        launchScenario<HomeActivity> {
            R.string.okt_routes_chip_label.clickWithText()

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
    fun givenOktRoute_whenClickOnLink_thenOktWebPageRequested() {
        launchScenario<HomeActivity> {
            R.string.okt_routes_chip_label.clickWithText()

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
            R.string.okt_routes_chip_label.clickWithText()

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
            R.string.okt_routes_chip_label.clickWithText()

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
            R.string.okt_routes_chip_label.clickWithText()

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
        launchScenario<HomeActivity> { scenario ->
            R.string.okt_routes_chip_label.clickWithText()

            scenario.recreate()
            waitForMapClear()

            R.string.okt_routes_bottom_sheet_title.isTextDisplayed()
            R.id.homeMapView.hasOverlay<MyLocationOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun waitForMapClear() {
        waitFor(300)
    }

}

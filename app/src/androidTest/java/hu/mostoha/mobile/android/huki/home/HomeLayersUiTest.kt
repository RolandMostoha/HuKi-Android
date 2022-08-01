package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.FileBasedHikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.ui.home.OverlayComparator
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.hasBaseTileSource
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import io.mockk.mockk
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class HomeLayersUiTest {

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
    val hikingLayerRepository: HikingLayerRepository = FileBasedHikingLayerRepository(testAppContext)

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun whenLayersButtonClick_thenLayersBottomSheetDialogShouldShown() {
        launchScenario<HomeActivity> {
            R.id.homeLayersFab.click()

            R.string.layers_base_layers_header.isTextDisplayed()
            R.string.layers_hiking_layers_header.isTextDisplayed()
        }
    }

    @Test
    fun whenSelectOpenTopoMap_thenOpenTopoLayerDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeMapView.hasBaseTileSource(TileSourceFactory.MAPNIK)

            R.id.homeLayersFab.click()

            onView(
                allOf(
                    withId(R.id.itemLayersImageCard),
                    hasSibling(withText(R.string.layers_open_topo_title))
                )
            ).perform(click())

            Espresso.pressBack()

            R.id.homeMapView.hasBaseTileSource(TileSourceFactory.OpenTopo)
        }
    }

    @Test
    fun whenMapOpens_thenHikingLayerDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeMapView.hasOverlay<TilesOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun whenDeselectHikingLayer_thenHikingLayerDoesNotDisplay() {
        launchScenario<HomeActivity> {
            R.id.homeLayersFab.click()

            onView(
                allOf(
                    withId(R.id.itemLayersImageCard),
                    hasSibling(withText(R.string.layers_hiking_hungarian_title))
                )
            ).perform(click())

            Espresso.pressBack()

            R.id.homeMapView.hasNoOverlay<TilesOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

}

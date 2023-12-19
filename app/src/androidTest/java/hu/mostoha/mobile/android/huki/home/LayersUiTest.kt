package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.MapnikTileSource
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithSibling
import hu.mostoha.mobile.android.huki.util.espresso.hasBaseTileSource
import hu.mostoha.mobile.android.huki.util.espresso.hasInvisibleOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class LayersUiTest {

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
            R.id.homeMapView.hasBaseTileSource(MapnikTileSource)

            R.id.homeLayersFab.click()
            R.id.itemLayersImageCard.clickWithSibling(R.string.layers_open_topo_title)
            pressBack()

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
            R.id.itemLayersImageCard.clickWithSibling(R.string.layers_hiking_hungarian_title)
            pressBack()

            R.id.homeMapView.hasNoOverlay<TilesOverlay>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenGpxFile_whenDeselectGpxLayer_thenGpxLayerDoesNotVisible() {
        Intents.init()

        launchScenario<HomeActivity> {
            Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.homeMapView.hasOverlay<GpxPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            R.id.homeLayersFab.click()
            R.id.itemLayersImageCard.clickWithSibling(R.string.layers_gpx_title)
            pressBack()

            R.id.homeMapView.hasInvisibleOverlay<GpxPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun getTestGpxFileResult(): Instrumentation.ActivityResult {
        val inputStream = testContext.assets.open("dera_szurdok.gpx")
        val file = File(testAppContext.cacheDir.path + "/dera_szurdok.gpx").apply {
            copyFrom(inputStream)
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            data = Uri.fromFile(file)
        }

        return Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
    }

}

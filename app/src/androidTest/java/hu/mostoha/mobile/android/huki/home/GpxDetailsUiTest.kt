package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
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
import hu.mostoha.mobile.android.huki.extensions.GOOGLE_MAPS_DIRECTIONS_URL
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithSibling
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlayCount
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
class GpxDetailsUiTest {

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
    val layersRepository: LayersRepository = FileBasedLayersRepository(testAppContext, LayersDomainModelMapper())

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
        Intents.init()

        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

    @Test
    fun whenImportGpxClicked_thenOpenFileIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            intended(
                allOf(
                    hasAction(Intent.ACTION_OPEN_DOCUMENT),
                    hasType("*/*")
                )
            )
        }
    }

    @Test
    fun givenGpxFile_whenImportGpxClicked_thenPolylineDisplaysOnMap() {
        launchScenario<HomeActivity> {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.homeMapView.hasOverlay<GpxPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenGpxFile_whenImportGpxClicked_thenGpxDetailsBottomSheetIsDisplayed() {
        launchScenario<HomeActivity> {
            R.id.homeGpxDetailsBottomSheetContainer.isNotDisplayed()

            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.homeGpxDetailsBottomSheetContainer.isDisplayed()
            R.id.gpxDetailsAltitudeDetailsContainer.isDisplayed()
            TEST_GPX_NAME.isTextDisplayed()
        }
    }

    @Test
    fun givenGpxFile_whenCloseClickOnBottomSheet_thenPolylineIsRemovedAndBottomSheetIsNotDisplayed() {
        launchScenario<HomeActivity> {
            R.id.homeGpxDetailsBottomSheetContainer.isNotDisplayed()

            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.gpxDetailsCloseButton.click()

            R.id.homeMapView.hasNoOverlay<GpxPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenGpxFile_whenNavigateToStartClicked_thenGoogleMapsDirectionsIntentIsFired() {
        launchScenario<HomeActivity> {
            R.id.homeGpxDetailsBottomSheetContainer.isNotDisplayed()

            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.gpxDetailsNavigateStartButton.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(
                        GOOGLE_MAPS_DIRECTIONS_URL.format(
                            TEST_GPX_START_LOCATION.latitude,
                            TEST_GPX_START_LOCATION.longitude
                        )
                    )
                )
            )
        }
    }

    @Test
    fun givenGpxFileWithoutAltitude_whenImportGpxClicked_thenGpxDetailsBottomSheetIsDisplayedWithoutAltitude() {
        launchScenario<HomeActivity> {
            R.id.homeGpxDetailsBottomSheetContainer.isNotDisplayed()

            val activityResult = getTestGpxFileResult(TEST_GPX_NAME_WITHOUT_ALTITUDE)
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.homeGpxDetailsBottomSheetContainer.isDisplayed()
            R.id.gpxDetailsAltitudeDetailsContainer.isNotDisplayed()
            TEST_GPX_NAME_WITHOUT_ALTITUDE.isTextDisplayed()
        }
    }

    @Test
    fun givenSecondGpxFile_whenImportGpxClicked_thenPreviousGpxIsCleared() {
        launchScenario<HomeActivity> {
            R.id.homeGpxDetailsBottomSheetContainer.isNotDisplayed()

            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            val activityResult = getTestGpxFileResult(TEST_GPX_NAME_WITHOUT_ALTITUDE)
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(activityResult)

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.homeMapView.hasOverlayCount<GpxPolyline>(1)
        }
    }

    @Test
    fun givenGpxDetails_whenRecreate_thenGpxDetailsDisplaysAgain() {
        launchScenario<HomeActivity> {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())
            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)

            R.id.homeMapView.hasOverlayCount<GpxPolyline>(1)
            R.id.homeGpxDetailsBottomSheetContainer.isDisplayed()
        }
    }

    private fun getTestGpxFileResult(fileName: String = TEST_GPX_NAME): Instrumentation.ActivityResult {
        val inputStream = testContext.assets.open("$fileName.gpx")
        val file = File(testAppContext.cacheDir.path + "/$fileName.gpx").apply {
            copyFrom(inputStream)
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            data = Uri.fromFile(file)
        }

        return Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
    }

    companion object {
        private const val TEST_GPX_NAME = "dera_szurdok"
        private const val TEST_GPX_NAME_WITHOUT_ALTITUDE = "dera_szurdok_without_altitude"
        private val TEST_GPX_START_LOCATION = Location(47.68498711287975, 18.91935557126999)
        private val DEFAULT_MY_LOCATION = Location(
            DEFAULT_MY_LOCATION_LATITUDE,
            DEFAULT_MY_LOCATION_LONGITUDE,
            DEFAULT_MY_LOCATION_ALTITUDE
        )
    }

}

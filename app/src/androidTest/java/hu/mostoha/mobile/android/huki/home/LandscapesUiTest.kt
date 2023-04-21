package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
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
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolygon
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.HikingRoutes.DEFAULT_HIKING_ROUTE
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_GEOMETRY_LANDSCAPE
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_LANDSCAPE
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.KIRANDULASTIPPEK_QUERY_URL
import hu.mostoha.mobile.android.huki.util.TERMESZETJARO_QUERY_URL
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
class LandscapesUiTest {

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
    }

    @Test
    fun givenLandscapes_whenClickOnLandscape_thenLandscapeDetailsBottomSheetDisplays() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            R.id.homeLandscapeDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.nameRes.clickWithText()

            R.id.homeLandscapeDetailsBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenLandscapes_whenClickOnLandscape_thenPolygonDisplaysOnMap() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            R.id.homeLandscapeDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.nameRes.clickWithText()

            R.id.homeMapView.hasOverlay<LandscapeMarker>()
            R.id.homeMapView.hasOverlay<LandscapePolygon>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenLandscape_whenClickOnHikingRoutes_thenHikingRoutesBottomSheetDisplays() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            landscape.nameRes.clickWithText()
            R.id.landscapeDetailsHikingTrailsButton.click()

            R.id.homeHikingRoutesBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenLandscape_whenClickOnKirandulastippek_thenUrlIntentIsFired() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            landscape.nameRes.clickWithText()
            R.id.landscapeDetailsKirandulastippekButton.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(KIRANDULASTIPPEK_QUERY_URL.format(landscape.kirandulastippekTag!!))
                )
            )
        }
    }

    @Test
    fun givenLandscape_whenClickOnTermeszetjaro_thenUrlIntentIsFired() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            landscape.nameRes.clickWithText()
            R.id.landscapeDetailsTermeszetjaroButton.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(TERMESZETJARO_QUERY_URL.format(testAppContext.getString(landscape.nameRes)))
                )
            )
        }
    }

    @Test
    fun whenRecreate_thenLandscapesAreDisplayedAgain() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> { scenario ->
            R.id.homeLandscapeDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.nameRes.isTextDisplayed()

            scenario.recreate()

            landscape.nameRes.isTextDisplayed()
        }
    }

    @Test
    fun givenLandscapeDetails_whenRecreate_thenLandscapeDetailsIsDisplayedAgain() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> { scenario ->
            landscape.nameRes.clickWithText()
            R.id.homeLandscapeDetailsBottomSheetContainer.isDisplayed()
            R.id.homeMapView.hasOverlay<LandscapeMarker>()
            R.id.homeMapView.hasOverlay<LandscapePolygon>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            scenario.recreate()

            R.id.homeMapView.hasOverlay<LandscapeMarker>()
            R.id.homeMapView.hasOverlay<LandscapePolygon>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

    private fun answerTestWayGeometry(id: String) {
        coEvery { placesRepository.getGeometry(id, any()) } returns DEFAULT_GEOMETRY_LANDSCAPE
        coEvery { placesRepository.getHikingRoutes(any()) } returns listOf(DEFAULT_HIKING_ROUTE)
    }

}

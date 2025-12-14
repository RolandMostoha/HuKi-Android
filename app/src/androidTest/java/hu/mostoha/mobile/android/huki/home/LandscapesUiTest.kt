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
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeDetailsDestinationMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeDetailsPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.HikingRoutes.DEFAULT_HIKING_ROUTE
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_GEOMETRY_LANDSCAPE
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_LANDSCAPE
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithSiblingContentDescription
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlayCount
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.swipeLeft
import hu.mostoha.mobile.android.huki.util.espresso.swipeUp
import hu.mostoha.mobile.android.huki.util.espresso.waitFor
import hu.mostoha.mobile.android.huki.util.espresso.waitForRecreate
import hu.mostoha.mobile.android.huki.util.espresso.waitForScroll
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder
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
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

    @BindValue
    @JvmField
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val geocodingRepository: GeocodingRepository = mockk()

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
            R.id.homePlaceCategoryBottomSheetContainer.isNotDisplayed()

            openLandscapeDetails(landscape)

            R.id.homePlaceCategoryBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenLandscapes_whenClickOnLandscape_thenPolygonDisplaysOnMap() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            openLandscapeDetails(landscape)

            R.id.homeMapView.hasOverlay<LandscapeDetailsPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenLandscapes_whenClickOnLandscape_thenDestinationsDisplaysOnMap() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            openLandscapeDetails(landscape)

            R.id.homeMapView.hasOverlayCount<LandscapeDetailsDestinationMarker>(landscape.destinations.count())
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenLandscape_whenClickOnHikingRoutes_thenHikingRoutesBottomSheetDisplays() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            openLandscapeDetails(landscape)

            R.id.placeCategoryBottomSheetHeaderContainer.swipeUp()
            waitForScroll()

            R.string.place_category_national_routes_chip_title.clickWithText()

            R.id.homeHikingRoutesBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenLandscape_whenClickOnKirandulastippek_thenUrlIntentIsFired() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            openLandscapeDetails(landscape)

            R.id.placeCategoryBottomSheetHeaderContainer.swipeUp()
            waitForScroll()

            R.string.hike_recommender_kirandulastippek.clickWithText()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(
                        HikeRecommendation.KIRANDULASTIPPEK.areaUrl.format(
                            landscape.areaTags[HikeRecommendation.KIRANDULASTIPPEK]
                        )
                    )
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
            openLandscapeDetails(landscape)

            R.id.placeCategoryBottomSheetHeaderContainer.swipeUp()
            waitForScroll()

            R.id.placeCategoryBottomSheetHikeRecommendationsScrollView.swipeLeft()
            R.string.hike_recommender_termeszetjaro.clickWithText()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(
                        HikeRecommendation.TERMESZETJARO.areaUrl.format(
                            landscape.termeszetjaroTag!!.areaId,
                            URLEncoder.encode(landscape.termeszetjaroTag.areaName, "UTF-8")
                        )
                    )
                )
            )
        }
    }

    @Test
    fun whenRecreate_thenLandscapesAreDisplayedAgain() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            R.id.homePlaceCategoryBottomSheetContainer.isNotDisplayed()

            R.id.homeDiscoverFab.click()
            R.string.discover_landscapes_button_title.clickWithText()

            recreate()
            waitForRecreate()

            landscape.nameRes.isTextDisplayed()
        }
    }

    @Test
    fun givenLandscapeDetails_whenRecreate_thenLandscapeDetailsIsDisplayedAgain() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            openLandscapeDetails(landscape)

            R.id.homePlaceCategoryBottomSheetContainer.isDisplayed()
            R.id.homeMapView.hasOverlay<LandscapeDetailsPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            recreate()
            waitForRecreate()

            R.id.homeMapView.hasOverlay<LandscapeDetailsPolyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun openLandscapeDetails(landscape: Landscape) {
        R.id.homeDiscoverFab.click()
        R.string.discover_landscapes_button_title.clickWithText()
        landscape.nameRes.clickWithText()
        waitFor(400)
        R.id.landscapesItemDetailsButton.clickWithSiblingContentDescription(testAppContext.getString(landscape.nameRes))
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

    private fun answerTestWayGeometry(id: String) {
        coEvery { placesRepository.getGeometry(id, any()) } returns DEFAULT_GEOMETRY_LANDSCAPE
        coEvery { placesRepository.getHikingRoutes(any()) } returns listOf(DEFAULT_HIKING_ROUTE)
    }

}

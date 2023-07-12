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
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_GEOMETRY_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_GEOMETRY_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_GEOMETRY_WAY
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_WAY
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_SEARCH_TEXT
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.GOOGLE_MAPS_DIRECTIONS_URL
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickInPopup
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
class PlacesUiTest {

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
        LayersDomainModelMapper(),
        HukiGpxConfiguration(testAppContext),
        FakeExceptionLogger(),
    )

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @BindValue
    @JvmField
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()

        answerTestLocationProvider()
    }

    @Test
    fun givenNodePlace_whenClickInSearchResults_thenPlaceDisplaysOnBottomSheet() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
            DEFAULT_PLACE_NODE.name.isTextDisplayed()
        }
    }

    @Test
    fun givenNodePlace_whenClickInSearchResults_thenDirectionsAndHikingTrailsButtonsDisplayedOnBottomSheet() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            R.id.placeDetailsGoogleNavButton.isDisplayed()
            R.string.home_bottom_sheet_route_plan_button.isTextDisplayed()
            R.string.home_bottom_sheet_show_points_button.isTextNotDisplayed()
        }
    }

    @Test
    fun givenNodePlace_whenClickInSearchResults_thenPlaceDisplaysOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenNodePlace_whenCloseClickOnBottomSheet_thenMarkerIsRemovedAndBottomSheetIsNotDisplayed() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeMapView.hasNoOverlay<Marker>()
        }
    }

    @Ignore("Flaky test of Google Maps intent check")
    @Test
    fun givenNodePlace_whenClickOnNavigation_thenGoogleMapsDirectionsIntentIsFired() {
        Intents.init()
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsGoogleNavButton.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(
                        GOOGLE_MAPS_DIRECTIONS_URL.format(
                            DEFAULT_PLACE_NODE.location.latitude,
                            DEFAULT_PLACE_NODE.location.longitude
                        )
                    )
                )
            )
        }
    }

    @Test
    fun givenSecondPlace_whenClickInSearchResults_thenSecondPlaceDisplaysOnBottomSheet() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
            DEFAULT_PLACE_NODE.name.isTextDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
            DEFAULT_PLACE_WAY.name.isTextDisplayed()
        }
    }

    @Ignore("Flaky test of Google Maps intent check")
    @Test
    fun givenNodePlace_whenClickDirections_thenGoogleMapsDirectionsIntentIsFired() {
        Intents.init()
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsGoogleNavButton.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(
                        GOOGLE_MAPS_DIRECTIONS_URL.format(
                            DEFAULT_PLACE_NODE.location.latitude,
                            DEFAULT_PLACE_NODE.location.longitude
                        )
                    )
                )
            )
        }
    }

    @Test
    fun givenWayPlace_whenClickInSearchResults_thenPlaceDisplaysOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenWayPlace_whenClickInSearchResults_thenBottomSheetButtonsDisplay() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.placeDetailsGoogleNavButton.isDisplayed()
            R.string.home_bottom_sheet_show_points_button.isTextDisplayed()
            R.string.home_bottom_sheet_route_plan_button.isTextDisplayed()
            R.string.home_bottom_sheet_hiking_trails_button.isTextNotDisplayed()
        }
    }

    @Test
    fun givenWayPlace_whenClickOnShowPointsButton_thenHikingTrailsButtonDisplaysOnBottomSheet() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithText()

            R.id.placeDetailsGoogleNavButton.isNotDisplayed()
            R.string.home_bottom_sheet_show_points_button.isTextNotDisplayed()
            R.string.home_bottom_sheet_hiking_trails_button.isTextDisplayed()
        }
    }

    @Test
    fun givenWayPlace_whenClickOnShowPointsButton_thenPlaceDetailsDisplayOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithText()

            R.id.homeMapView.hasOverlay<Polyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenRelationPlace_whenClickInSearchResults_thenPlaceDisplaysOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenRelationPlace_whenClickOnShowPointsButton_thenHikingTrailsButtonDisplaysOnBottomSheet() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithText()

            R.id.placeDetailsGoogleNavButton.isNotDisplayed()
            R.string.home_bottom_sheet_show_points_button.isTextNotDisplayed()
            R.string.home_bottom_sheet_hiking_trails_button.isTextDisplayed()
        }
    }

    @Test
    fun givenRelationPlace_whenClickOnShowPointsButton_thenPlaceDetailsDisplayOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithText()

            R.id.homeMapView.hasOverlay<Polyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenMyLocation_whenClickInPlaceFinder_thenPlaceDetailsDisplay() {
        answerTestPlaces()
        answerTestGeometries()
        answerTestLocationProvider()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)

            R.id.placeFinderMyLocationButton.clickInPopup()

            R.string.place_details_my_location_text.isTextDisplayed()
            LocationFormatter.format(DEFAULT_MY_LOCATION).text.isTextDisplayed()
            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenNodePlace_whenRecreate_thenPlaceDetailsDisplayOnMapAgain() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> { scenario ->
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            scenario.recreate()

            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun answerTestPlaces() {
        coEvery { placesRepository.getPlacesBy(any(), any()) } returns listOf(
            DEFAULT_PLACE_WAY,
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_RELATION
        )
    }

    private fun answerTestGeometries() {
        coEvery {
            placesRepository.getGeometry(DEFAULT_PLACE_NODE.osmId, any())
        } returns DEFAULT_GEOMETRY_NODE
        coEvery {
            placesRepository.getGeometry(DEFAULT_PLACE_WAY.osmId, any())
        } returns DEFAULT_GEOMETRY_WAY
        coEvery {
            placesRepository.getGeometry(DEFAULT_PLACE_RELATION.osmId, any())
        } returns DEFAULT_GEOMETRY_RELATION
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.startLocationProvider(any()) } returns true
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns DEFAULT_MY_LOCATION.toMockLocation()
    }

}

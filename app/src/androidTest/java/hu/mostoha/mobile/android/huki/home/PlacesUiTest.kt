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
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
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
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickInPopup
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextWithScroll
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.swipeLeft
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.espresso.waitForInputFocusGain
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
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
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

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

            R.string.home_bottom_sheet_route_plan_button.isTextDisplayed()
            R.id.placeDetailsFinderButton.isDisplayed()
            R.id.placeDetailsButtonGroupScrollView.swipeLeft()
            R.id.placeDetailsGoogleNavButton.isDisplayed()
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
            R.id.placeDetailsButtonGroupScrollView.swipeLeft()
            R.id.placeDetailsGoogleNavButton.click()
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

    @Test
    fun givenNodePlace_whenClickFinder_thenPlaceCategoryBottomSheetDisplays() {
        answerTestPlaces()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsFinderButton.click()

            R.id.homePlaceCategoryBottomSheetContainer.isDisplayed()
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
    fun givenWayPlace_whenClickInSearchResults_thenNodeBottomSheetButtonsDisplay() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.string.home_bottom_sheet_route_plan_button.isTextDisplayed()
            R.string.place_details_finder_button.isTextDisplayed()
            R.id.placeDetailsButtonGroupScrollView.swipeLeft()
            R.string.home_bottom_sheet_show_points_button.isTextDisplayed()
        }
    }

    @Test
    fun givenWayPlace_whenClickOnShowAllPointsButton_thenPolyPlaceDetailsBottomSheetDisplays() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithTextWithScroll()

            DEFAULT_PLACE_WAY.name.isTextDisplayed()
            R.id.placeDetailsButtonGroupScrollView.isNotDisplayed()
        }
    }

    @Test
    fun givenWayPlace_whenClickOnShowAllPointsButton_thenPlaceDetailsDisplayOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithTextWithScroll()

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
    fun givenRelationPlace_whenClickOnAllShowPointsButton_thenPolyPlaceDetailsBottomSheetDisplays() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithTextWithScroll()

            DEFAULT_PLACE_RELATION.name.isTextDisplayed()
            R.id.placeDetailsButtonGroupScrollView.isNotDisplayed()
        }
    }

    @Test
    fun givenRelationPlace_whenClickOnShowAllPointsButton_thenPlaceDetailsDisplayOnMap() {
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            R.string.home_bottom_sheet_show_points_button.clickWithTextWithScroll()

            R.id.homeMapView.hasOverlay<Polyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenMyLocation_whenClickInPlaceFinder_thenPlaceDetailsDisplaysWithCoordinates() {
        answerTestPlaces()
        answerTestGeometries()
        answerTestLocationProvider()

        coEvery { geocodingRepository.getPlace(any(), any()) } returns null

        launchScenario<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()

            R.id.placeFinderMyLocationButton.clickInPopup()

            R.string.place_details_my_location_text.isTextDisplayed()
            LocationFormatter.formatText(DEFAULT_MY_LOCATION).text.isTextDisplayed()
            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenMyLocationWithGeocoding_whenClickInPlaceFinder_thenPlaceDetailsDisplaysWithPlace() {
        answerTestPlaces()
        answerTestGeometries()
        answerTestLocationProvider()

        coEvery { geocodingRepository.getPlace(any(), any()) } returns DEFAULT_PLACE_NODE

        launchScenario<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()
            R.id.placeFinderMyLocationButton.clickInPopup()

            DEFAULT_PLACE_NODE.name.isTextDisplayed()
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
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(
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

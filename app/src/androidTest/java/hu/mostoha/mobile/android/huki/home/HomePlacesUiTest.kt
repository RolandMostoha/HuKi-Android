package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.extensions.GOOGLE_MAPS_DIRECTIONS_URL
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.FileBasedHikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_CENTER_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_CENTER_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_1_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_1_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_2_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_2_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.ui.home.OverlayComparator
import hu.mostoha.mobile.android.huki.util.espresso.click
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
import io.mockk.coEvery
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class HomePlacesUiTest {

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

            R.string.home_bottom_sheet_directions_button.isTextDisplayed()
            R.string.home_bottom_sheet_hiking_trails_button.isTextDisplayed()
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
            R.string.home_bottom_sheet_directions_button.clickWithText()

            intended(
                allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    IntentMatchers.hasData(
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

            R.string.home_bottom_sheet_directions_button.isTextDisplayed()
            R.string.home_bottom_sheet_show_points_button.isTextDisplayed()
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

            R.string.home_bottom_sheet_directions_button.isTextNotDisplayed()
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

            R.string.home_bottom_sheet_directions_button.isTextNotDisplayed()
            R.string.home_bottom_sheet_show_points_button.isTextNotDisplayed()
            R.string.home_bottom_sheet_hiking_trails_button.isTextDisplayed()
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

    companion object {
        private const val DEFAULT_SEARCH_TEXT = "Dobogoko"
        private val DEFAULT_PLACE_NODE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_GEOMETRY_NODE = Geometry.Node(
            osmId = DEFAULT_PLACE_NODE.osmId,
            location = DEFAULT_PLACE_NODE.location
        )
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            name = DEFAULT_WAY_NAME,
            placeType = PlaceType.WAY,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE)
        )
        private val DEFAULT_GEOMETRY_WAY = Geometry.Way(
            osmId = DEFAULT_PLACE_WAY.osmId,
            locations = DEFAULT_WAY_GEOMETRY.map { Location(it.first, it.second) },
            distance = 100
        )
        private val DEFAULT_PLACE_RELATION = Place(
            osmId = DEFAULT_RELATION_OSM_ID,
            name = DEFAULT_RELATION_NAME,
            placeType = PlaceType.RELATION,
            location = Location(DEFAULT_RELATION_CENTER_LATITUDE, DEFAULT_RELATION_CENTER_LONGITUDE)
        )
        private val DEFAULT_GEOMETRY_RELATION = Geometry.Relation(
            osmId = DEFAULT_PLACE_RELATION.osmId,
            ways = listOf(
                Geometry.Way(
                    osmId = DEFAULT_RELATION_WAY_1_OSM_ID,
                    locations = DEFAULT_RELATION_WAY_1_GEOMETRY.map { Location(it.first, it.second) },
                    distance = 1500
                ),
                Geometry.Way(
                    osmId = DEFAULT_RELATION_WAY_2_OSM_ID,
                    locations = DEFAULT_RELATION_WAY_2_GEOMETRY.map { Location(it.first, it.second) },
                    distance = 2000
                )
            )
        )
    }

}

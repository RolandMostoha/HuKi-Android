package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import hu.mostoha.mobile.android.huki.model.domain.OsmTags
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceCategoryMarker
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_GEOMETRY_LANDSCAPE
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_LANDSCAPE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_WAY
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_SEARCH_TEXT
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_PROFILE
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlayCount
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isSnackbarMessageDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.setBottomSheetState
import hu.mostoha.mobile.android.huki.util.espresso.swipeLeft
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.espresso.waitForBottomSheetState
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
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
class PlaceCategoryUiTest {

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
    }

    @Test
    fun whenClickOnHomePlaceCategories_thenPlaceCategoryDisplays() {
        launchScenario<HomeActivity> {
            R.id.homePlaceCategoriesFab.click()

            R.id.placeCategoryHeaderContainer.isDisplayed()
            R.id.placeCategoryLandscapeChipGroup.isDisplayed()
            R.id.placeCategoryHikeRecommendationsChipGroup.isDisplayed()
            R.id.placeCategoryGroups.isDisplayed()
        }
    }

    @Test
    fun givenNonGeocodedPlaceArea_whenClickOnHomePlaceCategories_thenAreMessageDisplays() {
        launchScenario<HomeActivity> {
            coEvery { geocodingRepository.getPlaceProfile(any()) } returns null

            R.id.homePlaceCategoriesFab.click()

            R.id.placeCategoryContainer.isDisplayed()

            // Default location on map
            "(47.0293,19.4549)".isTextDisplayed()
        }
    }

    @Test
    fun givenGeocodedPlaceArea_whenClickOnHomePlaceCategories_thenAreMessageDisplays() {
        launchScenario<HomeActivity> {
            coEvery { geocodingRepository.getPlaceProfile(any()) } returns DEFAULT_PLACE_PROFILE

            R.id.homePlaceCategoriesFab.click()

            "Hungary".isTextDisplayed()
        }
    }

    @Test
    fun givenPlaceCategory_whenClickCategoryOnHome_thenAreMessageDisplays() {
        launchScenario<HomeActivity> {
            answerTestPlaces()
            answerPlacesByCategories()

            R.id.homeSearchBarInput.typeText(DEFAULT_SEARCH_TEXT)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.homePlaceCategoriesFab.click()
            PlaceCategory.PEAK.title.clickWithText()

            R.id.homeMapView.hasOverlayCount<PlaceCategoryMarker>(2)
        }
    }

    @Test
    fun givenPlaceCategory_whenClickCategoryOnPlaceDetails_thenPlacesDisplayedByCategory() {
        launchScenario<HomeActivity> {
            answerTestPlaces()
            answerPlacesByCategories()

            R.id.homeSearchBarInput.typeText(DEFAULT_SEARCH_TEXT)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsFinderButton.click()

            R.id.homeContainer.setBottomSheetState(
                R.id.homePlaceCategoryBottomSheetContainer,
                BottomSheetBehavior.STATE_EXPANDED
            )
            waitForBottomSheetState()
            PlaceCategory.SHOP.title.clickWithText()

            R.id.homeMapView.hasOverlayCount<PlaceCategoryMarker>(1)
        }
    }

    @Test
    fun givenEmptyPlaces_whenClickCategory_thenSnackbarMessageDisplays() {
        launchScenario<HomeActivity> {
            launchScenario<HomeActivity> {
                answerTestPlaces()
                answerPlacesByCategories()

                R.id.homeSearchBarInput.typeText(DEFAULT_SEARCH_TEXT)
                DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
                R.id.homePlaceCategoriesFab.click()
                PlaceCategory.CASTLE.title.clickWithText()

                R.id.homeMapView.hasOverlayCount<PlaceCategoryMarker>(0)
                testAppContext.getString(
                    R.string.place_category_empty_message,
                    testAppContext.getString(
                        R.string.place_category_empty_message_category_template,
                        PlaceCategory.CASTLE.title.resolve(testAppContext)
                    )
                ).isSnackbarMessageDisplayed()
            }
        }
    }

    @Test
    fun whenClickOnLandscape_thenPlaceCategoryDisplays() {
        launchScenario<HomeActivity> {
            answerTestPlaces()
            answerPlacesByCategories()
            coEvery { placesRepository.getGeometry(any(), any()) } returns DEFAULT_GEOMETRY_LANDSCAPE

            R.id.homePlaceCategoriesFab.click()
            DEFAULT_LANDSCAPE.nameRes.clickWithText()

            R.id.homeContainer.setBottomSheetState(
                R.id.homePlaceCategoryBottomSheetContainer,
                BottomSheetBehavior.STATE_EXPANDED
            )
            waitForBottomSheetState()
            PlaceCategory.DRINKING_WATER.title.clickWithText()

            R.id.homeMapView.hasOverlayCount<PlaceCategoryMarker>(1)
        }
    }

    @Test
    fun givenOsmData_whenClickAllOsmData_thenOsmDataDisplaysInPopup() {
        launchScenario<HomeActivity> {
            answerTestPlaces()
            answerPlacesByCategories()
            val osmTags = mapOf(
                OsmTags.ELE.osmKey to "100",
                OsmTags.OPENING_HOURS.osmKey to "Mo-Fr 08:00-18:00",
                OsmTags.FEE.osmKey to "Yes",
            )
            coEvery { placesRepository.getOsmTags(any(), any()) } returns osmTags

            R.id.homeSearchBarInput.typeText(DEFAULT_SEARCH_TEXT)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsButtonGroupScrollView.swipeLeft()
            R.id.placeDetailsOsmDataButton.click()

            R.string.osm_data_popup_title.isPopupTextDisplayed()
            val osmIdPart = "OSM ID: ${DEFAULT_PLACE_NODE.osmId}\n"
            val osmTagsPart = osmTags.map { "${it.key}: ${it.value}" }.joinToString("\n")
            "$osmIdPart$osmTagsPart".isPopupTextDisplayed()
        }
    }

    @Test
    fun whenRecreate_thenPlaceCategoryDisplaysAgain() {
        launchScenario<HomeActivity> { scenario ->
            R.id.homePlaceCategoriesFab.click()
            R.id.placeCategoryHeaderContainer.isDisplayed()

            scenario.recreate()

            R.id.placeCategoryHeaderContainer.isDisplayed()
        }
    }

    private fun answerTestPlaces() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(
            DEFAULT_PLACE_WAY,
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_RELATION
        )
    }

    private fun answerPlacesByCategories() {
        coEvery { placesRepository.getPlacesByCategories(any(), any()) } returns categories
    }

    companion object {
        private val categories = listOf(
            DEFAULT_PLACE_NODE.copy(
                placeCategory = PlaceCategory.PEAK,
                osmTags = mapOf(OsmTags.ELE.osmKey to "100")
            ),
            DEFAULT_PLACE_NODE.copy(
                placeCategory = PlaceCategory.PEAK,
                name = "Rám-hegy".toMessage(),
                osmTags = mapOf(OsmTags.ELE.osmKey to "200")
            ),
            DEFAULT_PLACE_WAY.copy(
                placeCategory = PlaceCategory.SHOP,
                osmTags = mapOf(OsmTags.OPENING_HOURS.osmKey to "Mo-Fr 08:00-18:00")
            ),
            DEFAULT_PLACE_RELATION.copy(
                placeCategory = PlaceCategory.DRINKING_WATER,
                osmTags = mapOf(OsmTags.FEE.osmKey to "Yes")
            ),
        )
    }

}

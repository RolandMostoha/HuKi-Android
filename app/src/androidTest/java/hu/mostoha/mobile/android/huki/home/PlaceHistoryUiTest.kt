package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_ADDRESS
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_WAY
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_SEARCH_TEXT
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasDescendantWithText
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
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
class PlaceHistoryUiTest {

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
    val routPlannerRepository: RoutePlannerRepository = mockk()

    @BindValue
    @JvmField
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()

        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(DEFAULT_PLACE_NODE)
        answerTestLocationProvider()
        answerTestPlaces()
    }

    @Test
    fun whenOpenPlacesInFeatures_thenPlacesAreSavedInHistory() {
        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT

            // Map search 1
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            // Map search 2
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            // Route planner search 1
            R.id.homeRoutePlannerFab.click()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(searchText)))
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            R.id.routePlannerBackButton.click()

            R.id.homeHistoryFab.click()

            R.id.placeHistoryList.hasDescendantWithText(0, DEFAULT_PLACE_NODE.name)
            R.id.placeHistoryList.hasDescendantWithText(0, DEFAULT_PLACE_WAY.name)
            R.id.placeHistoryList.hasDescendantWithText(0, DEFAULT_PLACE_RELATION.name)
        }
    }

    @Test
    fun givenMorePlacesThanMax_whenOpenPlaceFinder_thenMaxNumberOfPlacesAndShowMoreIsDisplayed() {
        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT
            val landscapeSearchText = "Bukk"

            coEvery { geocodingRepository.getPlacesBy(landscapeSearchText, any(), any()) } returns listOf(
                DEFAULT_PLACE_LANDSCAPE
            )

            // Map search 1
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            // Map search 2
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            // Route planner search 1
            R.id.homeRoutePlannerFab.click()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(searchText)))
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()

            // Route planner search 2
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(landscapeSearchText)))
            DEFAULT_PLACE_LANDSCAPE.name.clickWithTextInPopup()
            R.id.routePlannerBackButton.click()

            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()

            R.string.place_finder_show_more_history.isPopupTextDisplayed()
        }
    }

    private fun answerTestPlaces() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_WAY,
            DEFAULT_PLACE_RELATION,
            DEFAULT_PLACE_LANDSCAPE
        )
        coEvery { geocodingRepository.getPlace(any(), any()) } returns null
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.startLocationProvider(any()) } returns true
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns DEFAULT_MY_LOCATION.toMockLocation()
    }

    companion object {
        private val DEFAULT_PLACE_LANDSCAPE = Place(
            osmId = DEFAULT_LANDSCAPE_OSM_ID,
            placeType = PlaceType.RELATION,
            name = DEFAULT_LANDSCAPE_NAME,
            address = DEFAULT_LANDSCAPE_ADDRESS,
            placeFeature = PlaceFeature.ROUTE_PLANNER_SEARCH,
            location = Location(DEFAULT_LANDSCAPE_LATITUDE, DEFAULT_LANDSCAPE_LONGITUDE)
        )
    }

}

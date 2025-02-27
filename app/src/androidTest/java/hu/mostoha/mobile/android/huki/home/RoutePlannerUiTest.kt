package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
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
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerPolyline
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_ADDRESS
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_CENTER_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_CENTER_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_PROFILE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_PROFILE_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_PROFILE_WAY
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_PROFILE
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithContentDescription
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasDisplayedItemAtPosition
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.waitFor
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(
    RepositoryModule::class,
    LocationModule::class,
    VersionConfigurationModule::class
)
class RoutePlannerUiTest {

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

        answerTestLocationProvider()
        coEvery { geocodingRepository.getAutocompletePlaces(any(), any()) } returns listOf(
            DEFAULT_PLACE_PROFILE_NODE,
            DEFAULT_PLACE_PROFILE_WAY,
            DEFAULT_PLACE_PROFILE_RELATION
        )
        coEvery {
            routPlannerRepository.getRoutePlan(
                listOf(
                    DEFAULT_PLACE_NODE.location,
                    DEFAULT_PLACE_WAY.location
                )
            )
        } returns DEFAULT_ROUTE_PLAN
        coEvery {
            routPlannerRepository.getRoutePlan(
                listOf(
                    DEFAULT_PLACE_NODE.location,
                    DEFAULT_PLACE_WAY.location,
                    DEFAULT_PLACE_RELATION.location
                )
            )
        } returns DEFAULT_ROUTE_PLAN_2
        coEvery {
            routPlannerRepository.getRoutePlan(
                listOf(
                    DEFAULT_PLACE_NODE.location,
                    DEFAULT_PLACE_WAY.location,
                    DEFAULT_PLACE_NODE.location
                )
            )
        } returns DEFAULT_ROUTE_PLAN_2
        coEvery {
            routPlannerRepository.getRoutePlan(
                listOf(
                    DEFAULT_MY_LOCATION.copy(altitude = null),
                    DEFAULT_PLACE_NODE.location,
                )
            )
        } returns DEFAULT_ROUTE_PLAN
        coEvery { routPlannerRepository.saveRoutePlan(any()) } returns getTestGpxFileUri()
    }

    @Test
    fun whenRoutePlannerFabIsClicked_thenRoutePlannerDisplaysAndFabHides() {
        launchScenario<HomeActivity> {
            R.id.homeRoutePlannerFab.click()

            R.id.homeRoutePlannerContainer.isDisplayed()
            R.id.routePlannerDoneButton.isDisplayed()
            R.id.routePlannerGraphhopperContainer.isDisplayed()
            R.id.homeRoutePlannerFab.isNotDisplayed()
        }
    }

    @Test
    fun whenRoutePlannerBackButtonIsClicked_thenRoutePlannerHidesAndFabIsDisplayed() {
        launchScenario<HomeActivity> {
            R.id.homeRoutePlannerFab.click()

            R.id.routePlannerBackButton.click()

            R.id.homeRoutePlannerContainer.isNotDisplayed()
            R.id.homeRoutePlannerFab.isDisplayed()
        }
    }

    @Test
    fun givenPlaceFinderOpen_whenClickInMap_thenWaypointInputIsCleared() {
        launchScenario<HomeActivity> {
            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText("as")))
                .check(matches(hasFocus()))

            R.id.homeMapView.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(scrollToPosition<ViewHolder>(0))
                .check(matches(not(hasFocus())))
        }
    }

    @Test
    fun givenWaypoints_whenWaypointsAreFilled_thenRoutePlanDisplays() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.routePlannerRouteAttributesContainer.isDisplayed()
            "13 km".isTextDisplayed()
            "500 m".isTextDisplayed()
            "200 m".isTextDisplayed()
            "01:40".isTextDisplayed()
        }
    }

    @Test
    fun givenWaypoints_whenAddNewWaypoint_thenRoutePlanUpdates() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"
            val waypointName3 = "Fuzerkomlos"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            waitForPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            waitForPopup()

            R.id.routePlannerAddWaypointButton.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(2, typeText(waypointName3)))
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            waitForPopup()

            R.id.routePlannerRouteAttributesContainer.isDisplayed()
            "15 km".isTextDisplayed()
            "600 m".isTextDisplayed()
            "300 m".isTextDisplayed()
            "02:00".isTextDisplayed()
        }
    }

    @Test
    fun givenTwoWaypoints_whenReturnToHomeClicked_thenFirstPositionIsAdded() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            waitForPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            waitForPopup()

            R.id.routePlannerReturnToHomeButton.click()

            R.id.routePlannerWaypointList.hasDisplayedItemAtPosition(2)
        }
    }

    @Test
    fun givenClosedWaypoints_thenReturnToHomeIsNotDisplayed() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            waitForPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            waitForPopup()

            R.id.routePlannerReturnToHomeButton.click()

            R.id.routePlannerReturnToHomeButton.isNotDisplayed()
        }
    }

    @Test
    fun givenMyLocation_whenAddMyLocationWaypoint_thenRoutePlanUpdates() {
        launchScenario<HomeActivity> {
            coEvery { geocodingRepository.getPlaceProfile(any()) } returns null

            val waypointName1 = "Dobogoko"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, click()))
            R.string.place_finder_my_location_button.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            R.id.routePlannerRouteAttributesContainer.isDisplayed()
            "13 km".isTextDisplayed()
            "500 m".isTextDisplayed()
            "200 m".isTextDisplayed()
            "01:40".isTextDisplayed()
        }
    }

    @Test
    fun givenMyLocationWithGeocoding_whenAddMyLocationWaypoint_thenRoutePlanUpdates() {
        launchScenario<HomeActivity> {
            coEvery { geocodingRepository.getPlaceProfile(any()) } returns DEFAULT_PLACE_PROFILE
            coEvery { routPlannerRepository.getRoutePlan(any()) } returns DEFAULT_ROUTE_PLAN

            val waypointName1 = "Dobogoko"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, click()))
            R.string.place_finder_my_location_button.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName1)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.routePlannerRouteAttributesContainer.isDisplayed()
            "13 km".isTextDisplayed()
            "500 m".isTextDisplayed()
            "200 m".isTextDisplayed()
            "01:40".isTextDisplayed()
        }
    }

    @Test
    fun whenAddMaxNumberOfWaypoints_thenAdditionIsDisabled() {
        launchScenario<HomeActivity> {
            R.id.homeRoutePlannerFab.click()

            repeat(10) {
                R.id.routePlannerAddWaypointButton.click()
            }

            R.id.routePlannerAddWaypointButton.isNotDisplayed()
        }
    }

    @Test
    fun givenWaypoints_whenRemoveWaypoint_thenRoutePlanUpdates() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"
            val waypointName3 = "Fuzerkomlos"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            waitForPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            waitForPopup()

            R.id.routePlannerAddWaypointButton.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(2, typeText(waypointName3)))
            DEFAULT_PLACE_RELATION.name.clickWithTextInPopup()
            waitForPopup()

            R.string.route_planner_accessibility_remove_waypoint.clickWithContentDescription()

            R.id.routePlannerRouteAttributesContainer.isDisplayed()
            "13 km".isTextDisplayed()
            "500 m".isTextDisplayed()
            "200 m".isTextDisplayed()
            "01:40".isTextDisplayed()
        }
    }

    @Test
    fun givenError_whenCreateRoutePlan_thenErrorViewDisplays() {
        launchScenario<HomeActivity> {
            val exception = DomainException(
                R.string.route_planner_general_error_message.toMessage(),
                IllegalStateException("")
            )
            coEvery { routPlannerRepository.getRoutePlan(any()) } throws exception

            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.routePlannerErrorText.isDisplayed()
        }
    }

    @Test
    fun givenRoutePlan_whenBackButtonClicked_thenRoutePlanHidesOnMap() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlay<RoutePlannerPolyline>()

            R.id.routePlannerBackButton.click()
            R.id.homeMapView.hasNoOverlay<RoutePlannerPolyline>()
        }
    }

    @Test
    fun givenRoutePlan_whenSaveButtonClicked_thenGpxDisplaysOnMap() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.routePlannerDoneButton.click()

            R.id.homeMapView.hasOverlay<GpxPolyline>()
        }
    }

    @Test
    fun whenGraphhopperContainerIsClicked_thenGraphhopperWebsiteDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeRoutePlannerFab.click()

            R.id.routePlannerGraphhopperContainer.click()

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(testAppContext.getString(R.string.route_planner_graphhopper_url))
                )
            )
        }
    }

    @Test
    fun whenRecreate_thenRoutePlannerIsDisplayedAgain() {
        launchScenario<HomeActivity> { scenario ->
            R.id.homeRoutePlannerFab.click()

            R.id.routePlannerContainer.isDisplayed()

            scenario.recreate()

            R.id.routePlannerContainer.isDisplayed()
        }
    }

    private fun waitForPopup() {
        waitFor(300)
    }

    private fun getTestGpxFileUri(): Uri {
        val inputStream = testContext.assets.open("dera_szurdok.gpx")
        val file = File(testAppContext.cacheDir.path + "/dera_szurdok.gpx").apply {
            copyFrom(inputStream)
        }

        return Uri.fromFile(file)
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.startLocationProvider(any()) } returns true
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns DEFAULT_MY_LOCATION.toMockLocation()
    }

    companion object {
        private val DEFAULT_PLACE_NODE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            name = DEFAULT_NODE_NAME.toMessage(),
            fullAddress = DEFAULT_NODE_CITY,
            placeFeature = PlaceFeature.MAP_SEARCH,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            placeType = PlaceType.WAY,
            name = DEFAULT_WAY_NAME.toMessage(),
            fullAddress = DEFAULT_WAY_CITY,
            placeFeature = PlaceFeature.MAP_SEARCH,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE)
        )
        private val DEFAULT_PLACE_RELATION = Place(
            osmId = DEFAULT_RELATION_OSM_ID,
            placeType = PlaceType.RELATION,
            name = DEFAULT_RELATION_NAME.toMessage(),
            fullAddress = DEFAULT_RELATION_ADDRESS,
            placeFeature = PlaceFeature.MAP_SEARCH,
            location = Location(DEFAULT_RELATION_CENTER_LATITUDE, DEFAULT_RELATION_CENTER_LONGITUDE)
        )
        private val DEFAULT_WAYPOINTS = listOf(
            Location(
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
            ),
            Location(
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
            )
        )
        private val DEFAULT_ROUTE_PLAN = RoutePlan(
            wayPoints = DEFAULT_WAYPOINTS,
            locations = DEFAULT_WAYPOINTS,
            travelTime = 100L.minutes,
            distance = 13000,
            altitudeRange = Pair(
                DEFAULT_WAYPOINTS.minOf { it.altitude!! }.toInt(),
                DEFAULT_WAYPOINTS.maxOf { it.altitude!! }.toInt()
            ),
            incline = 500,
            decline = 200,
            isClosed = false
        )
        private val DEFAULT_ROUTE_PLAN_2 = RoutePlan(
            wayPoints = DEFAULT_WAYPOINTS,
            locations = DEFAULT_WAYPOINTS,
            travelTime = 120L.minutes,
            distance = 15000,
            altitudeRange = Pair(
                DEFAULT_WAYPOINTS.minOf { it.altitude!! }.toInt(),
                DEFAULT_WAYPOINTS.maxOf { it.altitude!! }.toInt()
            ),
            incline = 600,
            decline = 300,
            isClosed = true
        )
    }

}

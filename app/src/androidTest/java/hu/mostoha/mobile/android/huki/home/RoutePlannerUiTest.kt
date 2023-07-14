package hu.mostoha.mobile.android.huki.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
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
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerPolyline
import hu.mostoha.mobile.android.huki.repository.*
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.*
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
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
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
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
        coEvery { placesRepository.getPlacesBy(any(), any()) } returns listOf(
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_WAY,
            DEFAULT_PLACE_RELATION
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

            R.string.route_planner_accessibility_add_waypoint.clickWithContentDescription()

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
    fun givenMyLocation_whenAddMyLocationWaypoint_thenRoutePlanUpdates() {
        launchScenario<HomeActivity> {
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
    fun whenAdd6NewWaypoints_thenAdditionIsDisabled() {
        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"

            R.id.homeRoutePlannerFab.click()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(0, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(1, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.string.route_planner_accessibility_add_waypoint.clickWithContentDescription()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(2, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            R.string.route_planner_accessibility_add_waypoint.clickWithContentDescription()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<ViewHolder>(3, typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            onView(withId(R.id.routePlannerWaypointList))
                .perform(scrollToPosition<ViewHolder>(2))
            R.string.route_planner_accessibility_add_waypoint.clickWithContentDescription()

            R.string.route_planner_accessibility_add_waypoint.doesNotExistWithContentDescription()
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

            R.string.route_planner_accessibility_add_waypoint.clickWithContentDescription()

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
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            name = DEFAULT_WAY_NAME,
            placeType = PlaceType.WAY,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE)
        )
        private val DEFAULT_PLACE_RELATION = Place(
            osmId = DEFAULT_RELATION_OSM_ID,
            name = DEFAULT_RELATION_NAME,
            placeType = PlaceType.RELATION,
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

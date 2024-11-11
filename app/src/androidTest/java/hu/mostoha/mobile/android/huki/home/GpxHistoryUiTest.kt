package hu.mostoha.mobile.android.huki.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.Gpx.TEST_GPX_NAME
import hu.mostoha.mobile.android.huki.testdata.Gpx.getTestGpxFileResult
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_WAY
import hu.mostoha.mobile.android.huki.testdata.RoutePlanner.DEFAULT_ROUTE_PLAN
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.ViewPagerIdlingResource
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithSibling
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.selectTab
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(
    RepositoryModule::class,
    VersionConfigurationModule::class
)
class GpxHistoryUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    private val gpxConfiguration = HukiGpxConfiguration(testAppContext)

    @BindValue
    @JvmField
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

    @BindValue
    @JvmField
    val layersRepository: LayersRepository = FileBasedLayersRepository(
        testContext,
        UnconfinedTestDispatcher(),
        LayersDomainModelMapper(),
        gpxConfiguration,
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

    private lateinit var viewPagerIdlingResource: ViewPagerIdlingResource

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()

        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_WAY,
            DEFAULT_PLACE_RELATION
        )
        coEvery { routPlannerRepository.getRoutePlan(any()) } returns DEFAULT_ROUTE_PLAN
        coEvery { routPlannerRepository.saveRoutePlan(any()) } returns getTestRoutePlannerGpxUri()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(viewPagerIdlingResource)
    }

    @Test
    fun whenGpxHistoryFabIsClicked_thenGpxHistoryDisplays() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(1)

            R.id.historyContainer.isDisplayed()
        }
    }

    @Test
    fun givenEmptyRoutePlannerGpxList_whenGpxHistoryOpens_thenEmptyViewDisplays() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)

            R.id.historyTabLayout.selectTab(1)

            R.string.gpx_history_item_route_planner_empty.isTextDisplayed()
        }
    }

    @Test
    fun givenImportedGpx_whenGpxHistoryOpens_thenGpxFileDisplays() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)
            R.id.gpxDetailsCloseButton.click()

            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(2)

            R.id.gpxHistoryItemContainer.isDisplayed()
        }
    }

    @Test
    fun givenExternalGpxFileInHistory_whenClickOpen_thenGpxDetailsDisplays() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)
            R.id.gpxDetailsCloseButton.click()

            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(2)
            R.id.gpxHistoryItemContainer.click()

            R.id.gpxDetailsStartButton.isDisplayed()
        }
    }

    @Test
    fun givenRoutePlannerGpxFileInHistory_whenClickOpen_thenGpxDetailsDisplays() {
        gpxConfiguration.clearAllGpxFiles()
        getTestRoutePlannerGpxUri()

        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, ViewActions.typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.routePlannerDoneButton.click()

            R.id.gpxDetailsCloseButton.click()

            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(1)
            R.id.gpxHistoryItemContainer.click()

            R.id.gpxDetailsStartButton.isDisplayed()
        }
    }

    @Test
    fun givenRoutePlannerGpxFileInHistory_whenClickDelete_thenListItemIsRemoved() {
        gpxConfiguration.clearAllGpxFiles()
        getTestRoutePlannerGpxUri()

        launchScenario<HomeActivity> {
            val waypointName1 = "Dobogoko"
            val waypointName2 = "Ram-hegy"

            R.id.homeRoutePlannerFab.click()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.typeText(waypointName1)))
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()
            onView(withId(R.id.routePlannerWaypointList))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, ViewActions.typeText(waypointName2)))
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.routePlannerDoneButton.click()

            R.id.gpxDetailsCloseButton.click()

            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(1)
            R.id.gpxHistoryActionsButton.click()
            R.string.gpx_history_menu_action_delete.clickWithTextInPopup()

            R.string.gpx_history_item_route_planner_empty.isTextDisplayed()
        }
    }

    @Test
    fun givenRoutePlannerGpxFileInHistory_whenClickRename_thenListItemIsUpdated() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)
            R.id.gpxDetailsCloseButton.click()

            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(2)
            R.id.gpxHistoryActionsButton.click()
            R.string.gpx_history_menu_action_rename.clickWithTextInPopup()
            R.id.gpxRenameInput.typeText("New file name")
            R.id.gpxRenameSaveButton.click()

            "New file name.gpx".isTextDisplayed()
        }
    }

    @Test
    fun givenRoutePlannerGpxFileInHistory_whenShareClicked_thenFileShareIsRequested() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)
            R.id.gpxDetailsCloseButton.click()

            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(2)
            R.id.gpxHistoryActionsButton.click()
            R.string.gpx_history_menu_action_share.clickWithTextInPopup()
        }
    }

    @Test
    fun whenBackButtonIsClicked_thenHomeDisplays() {
        gpxConfiguration.clearAllGpxFiles()

        launchScenario<HomeActivity> {
            R.id.homeHistoryFab.click()
            R.id.historyViewPager.registerViewPagerIdlingResource(this)
            R.id.historyTabLayout.selectTab(1)

            onView(
                allOf(
                    instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.historyToolbar))
                )
            ).perform(click())

            R.id.homeMapView.isDisplayed()
        }
    }

    private fun getTestRoutePlannerGpxUri(): Uri {
        val inputStream = testContext.assets.open(TEST_GPX_NAME)
        val file = File(gpxConfiguration.getRoutePlannerGpxDirectory() + "/$TEST_GPX_NAME").apply {
            copyFrom(inputStream)
        }

        return Uri.fromFile(file)
    }

    private fun <T : Activity> @receiver:IdRes Int.registerViewPagerIdlingResource(scenario: ActivityScenario<T>) {
        scenario.onActivity {
            viewPagerIdlingResource = ViewPagerIdlingResource(it.findViewById(this))
            IdlingRegistry.getInstance().register(viewPagerIdlingResource)
        }
    }

}

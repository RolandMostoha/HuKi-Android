package hu.mostoha.mobile.android.huki.home

import android.content.Intent
import android.net.Uri
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.*
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.testdata.Gpx.TEST_GPX_NAME
import hu.mostoha.mobile.android.huki.testdata.Gpx.getTestGpxFileResult
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_RELATION
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_WAY
import hu.mostoha.mobile.android.huki.testdata.RoutePlanner.DEFAULT_ROUTE_PLAN
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.*
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.coEvery
import io.mockk.mockk
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class GpxHistoryUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    private val gpxConfiguration = HukiGpxConfiguration(testAppContext)

    @BindValue
    @JvmField
    val layersRepository: LayersRepository = FileBasedLayersRepository(
        testContext,
        LayersDomainModelMapper(),
        gpxConfiguration,
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

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
        Intents.init()

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
        coEvery { routPlannerRepository.saveRoutePlan(any()) } returns getTestRoutePlannerGpxUri()
    }

    @Test
    fun whenGpxHistoryFabIsClicked_thenGpxHistoryDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeGpxHistoryFab.click()

            R.id.gpxHistoryContainer.isDisplayed()
        }
    }

    @Test
    fun givenEmptyRoutePlannerGpxList_whenGpxHistoryOpens_thenEmptyViewDisplays() {
        gpxConfiguration.clearAllGpxFiles()
        launchScenario<HomeActivity> {
            R.id.homeGpxHistoryFab.click()

            R.string.gpx_history_item_route_planner_empty.isTextDisplayed()
        }
    }

    @Test
    fun givenImportedGpx_whenGpxHistoryOpens_thenGpxFileDisplays() {
        launchScenario<HomeActivity> {
            intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)
            R.id.gpxDetailsCloseButton.click()

            R.id.homeGpxHistoryFab.click()
            R.id.gpxHistoryTabLayout.selectTab(1)

            R.id.gpxHistoryItemOpenButton.isDisplayed()
        }
    }

    @Test
    fun givenExternalGpxFileInHistory_whenClickOpen_thenGpxDetailsDisplays() {
        launchScenario<HomeActivity> {
            intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(getTestGpxFileResult())

            R.id.homeLayersFab.click()
            R.id.itemLayersActionButton.clickWithSibling(R.string.layers_gpx_title)
            R.id.gpxDetailsCloseButton.click()

            R.id.homeGpxHistoryFab.click()
            R.id.gpxHistoryTabLayout.selectTab(1)
            R.id.gpxHistoryItemOpenButton.click()

            R.id.gpxDetailsStartButton.isDisplayed()
        }
    }

    @Test
    fun givenRoutePlannerGpxFileInHistory_whenClickOpen_thenGpxDetailsDisplays() {
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

            R.id.homeGpxHistoryFab.click()
            R.id.gpxHistoryItemOpenButton.click()

            R.id.gpxDetailsStartButton.isDisplayed()
        }
    }

    @Test
    fun whenBackButtonIsClicked_thenHomeDisplays() {
        launchScenario<HomeActivity> {
            R.id.homeGpxHistoryFab.click()

            onView(
                allOf(
                    instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.gpxHistoryToolbar))
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

}

package hu.mostoha.mobile.android.huki.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.FileBasedHikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_JEL
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.MAP_ZOOM_THRESHOLD_ROUTES_NEARBY
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.waitFor
import hu.mostoha.mobile.android.huki.util.espresso.zoomTo
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class HomeHikingRoutesUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

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
    fun givenZoomLevelInThreshold_whenZoomOut_thenRoutesNearbyIsNotVisible() {

        launchScenario<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY - 1)
            waitForFabAnimation()

            R.id.homeRoutesNearbyFab.isNotDisplayed()

            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY + 1)
            waitForFabAnimation()

            R.id.homeRoutesNearbyFab.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoutes_whenClickRoutesNearby_thenHikingRoutesDisplayOnBottomSheet() {
        answerTestHikingRoute()

        launchScenario<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)

            waitForFabAnimation()

            R.id.homeRoutesNearbyFab.click()

            DEFAULT_HIKING_ROUTE.name.isTextDisplayed()
        }
    }

    @Test
    fun givenEmptyHikingRoutes_whenClickRoutesNearby_thenEmptyViewDisplayOnBottomSheet() {
        coEvery { placesRepository.getHikingRoutes(any()) } returns emptyList()

        launchScenario<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)
            waitForFabAnimation()
            R.id.homeRoutesNearbyFab.click()

            R.id.hikingRoutesEmptyView.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoute_whenClickOnRouteInBottomSheet_thenHikingRouteDetailsDisplayOnBottomSheet() {
        answerTestGeometries()
        answerTestHikingRoute()

        launchScenario<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)

            waitForFabAnimation()

            R.id.homeRoutesNearbyFab.click()

            DEFAULT_HIKING_ROUTE.name.clickWithText()

            R.id.homeRoutesNearbyFab.isDisplayed()
        }
    }

    private fun answerTestHikingRoute() {
        coEvery { placesRepository.getHikingRoutes(any()) } returns listOf(DEFAULT_HIKING_ROUTE)
    }

    private fun answerTestGeometries() {
        coEvery {
            placesRepository.getGeometry(DEFAULT_HIKING_ROUTE.osmId, any())
        } returns DEFAULT_HIKING_ROUTE_GEOMETRY
    }

    private fun waitForFabAnimation() {
        waitFor(400)
    }

    companion object {
        private val DEFAULT_HIKING_ROUTE = HikingRoute(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolType = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
        )
        private val DEFAULT_HIKING_ROUTE_GEOMETRY = Geometry.Relation(
            osmId = DEFAULT_HIKING_ROUTE.osmId,
            ways = listOf(
                Geometry.Way(
                    osmId = DEFAULT_HIKING_ROUTE_WAY_OSM_ID,
                    locations = DEFAULT_HIKING_ROUTE_WAY_GEOMETRY.map { Location(it.first, it.second) },
                    distance = 8500
                )
            )
        )
    }

}

package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.ServiceModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.MAP_ZOOM_THRESHOLD_ROUTES_NEARBY
import hu.mostoha.mobile.android.huki.util.espresso.*
import hu.mostoha.mobile.android.huki.util.launch
import hu.mostoha.mobile.android.huki.util.testContext
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
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class MapHikingRoutesUseCaseTest {

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
    val hikingLayerRepository: HikingLayerRepository = mockk()

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
        answerTestHikingLayer()

        launch<HomeActivity> {
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
        answerTestHikingLayer()
        answerTestHikingRoute()

        launch<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)

            waitForFabAnimation()

            R.id.homeRoutesNearbyFab.click()

            DEFAULT_HIKING_ROUTE.name.isTextDisplayed()
        }
    }

    @Test
    fun givenEmptyHikingRoutes_whenClickRoutesNearby_thenEmptyViewDisplayOnBottomSheet() {
        answerTestHikingLayer()
        coEvery { placesRepository.getHikingRoutes(any()) } returns emptyList()

        launch<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)
            waitForFabAnimation()
            R.id.homeRoutesNearbyFab.click()

            R.id.hikingRoutesEmptyView.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoute_whenClickOnRouteInBottomSheet_thenHikingRouteDetailsDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestGeometries()
        answerTestHikingRoute()

        launch<HomeActivity> {
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

    private fun answerTestHikingLayer() {
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

    private fun waitForFabAnimation() {
        waitFor(300)
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

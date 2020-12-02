package hu.mostoha.mobile.android.turistautak.home

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.constants.MAP_ZOOM_THRESHOLD_ROUTES_NEARBY
import hu.mostoha.mobile.android.turistautak.di.module.RepositoryModule
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.extensions.copyFrom
import hu.mostoha.mobile.android.turistautak.model.domain.HikingRoute
import hu.mostoha.mobile.android.turistautak.model.network.SymbolType
import hu.mostoha.mobile.android.turistautak.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.repository.HikingLayerRepository
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import hu.mostoha.mobile.android.turistautak.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.turistautak.repository.PlacesRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.espresso.*
import hu.mostoha.mobile.android.turistautak.util.launch
import hu.mostoha.mobile.android.turistautak.util.testContext
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
class MapRoutesNearbyUseCaseTest {

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
    val layerRepository: HikingLayerRepository = mockk()

    @BindValue
    @JvmField
    val placeRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun whenZoomLevelAboveThreshold_thenRoutesNearbyVisible() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY - 1)

            R.id.homeRoutesNearbyFab.isNotDisplayed()

            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)

            R.id.homeRoutesNearbyFab.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoutes_whenClickRoutesNearby_thenHikingRoutesDisplayOnBottomSheet() {
        answerTestHikingLayer()
        coEvery { placeRepository.getHikingRoutes(any()) } returns listOf(
            HikingRoute("1", "Írott-kő - Budapest - Hollóháza", SymbolType.Z),
            HikingRoute("2", "Országos Kéktúra 19. - Becske–Mátraverebély", SymbolType.K)
        )

        launch<HomeActivity> {
            R.id.homeMapView.zoomTo(MAP_ZOOM_THRESHOLD_ROUTES_NEARBY)
            R.id.homeRoutesNearbyFab.click()

            "Írott-kő - Budapest - Hollóháza".isTextDisplayed()
        }
    }

    private fun answerTestHikingLayer() {
        coEvery { layerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

}
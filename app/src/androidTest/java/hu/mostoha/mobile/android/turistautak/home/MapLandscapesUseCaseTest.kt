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
import hu.mostoha.mobile.android.turistautak.data.landscapes
import hu.mostoha.mobile.android.turistautak.di.module.RepositoryModule
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.extensions.copyFrom
import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.model.network.SymbolType
import hu.mostoha.mobile.android.turistautak.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.repository.HikingLayerRepository
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import hu.mostoha.mobile.android.turistautak.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.turistautak.repository.PlacesRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.espresso.click
import hu.mostoha.mobile.android.turistautak.util.espresso.clickWithText
import hu.mostoha.mobile.android.turistautak.util.espresso.isDisplayed
import hu.mostoha.mobile.android.turistautak.util.espresso.isNotDisplayed
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
class MapLandscapesUseCaseTest {

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
    fun whenClickOnLandscape_thenPlaceDetailsDisplayOnBottomSheet() {
        val landscape = landscapes.first()
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscape.id)

        launch<HomeActivity> {
            R.id.placeDetailsContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()

            R.id.placeDetailsContainer.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoutesInLandscapes_whenClickHikingTrails_thenHikingRoutesDisplayOnBottomSheet() {
        val landscape = landscapes.first()
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscape.id)
        coEvery { placeRepository.getHikingRoutes(any()) } returns listOf(
            HikingRoute("1", "Írott-kő - Budapest - Hollóháza", SymbolType.Z),
            HikingRoute("2", "Országos Kéktúra 19. - Becske–Mátraverebély", SymbolType.K)
        )

        launch<HomeActivity> {
            R.id.placeDetailsContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()
            R.id.placeDetailsHikingTrailsButton.click()

            R.id.hikingRoutesList.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoute_whenClick_thenHikingRouteDetailsDisplayOnBottomSheet() {
        val landscape = landscapes.first()
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscape.id)
        coEvery { placeRepository.getHikingRoutes(any()) } returns listOf(
            HikingRoute("1", "Írott-kő - Budapest - Hollóháza", SymbolType.Z),
            HikingRoute("2", "Országos Kéktúra 19. - Becske–Mátraverebély", SymbolType.K)
        )
        coEvery { placeRepository.getPlaceDetails("1", PlaceType.RELATION) } returns PlaceDetails(
            id = "1",
            payLoad = PayLoad.Relation(
                ways = listOf(
                    PayLoad.Way(
                        id = "1",
                        locations = listOf(Location(47.123, 19.124)),
                        distance = 5000
                    )
                )
            )
        )

        launch<HomeActivity> {
            landscape.name.clickWithText()
            R.id.placeDetailsHikingTrailsButton.click()

            "Írott-kő - Budapest - Hollóháza".clickWithText()

            R.id.placeDetailsContainer.isDisplayed()
            R.id.hikingRoutesList.isNotDisplayed()
        }
    }

    private fun answerTestPlaceDetailsWay(id: String) {
        coEvery { placeRepository.getPlaceDetails(id, any()) } returns PlaceDetails(
            id = id,
            payLoad = PayLoad.Way(
                id = id,
                locations = listOf(Location(47.123, 19.124)),
                distance = 5000
            )
        )
    }

    private fun answerTestHikingLayer() {
        coEvery { layerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

}
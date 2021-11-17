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
import hu.mostoha.mobile.android.huki.data.landscapes
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.ServiceModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
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
    fun whenClickOnLandscape_thenPlaceDetailsDisplayOnBottomSheet() {
        val landscape = landscapes.first()
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscape.osmId)

        launch<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoutesInLandscapes_whenClickHikingTrails_thenHikingRoutesDisplayOnBottomSheet() {
        val landscape = landscapes.first()
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscape.osmId)
        coEvery { placesRepository.getHikingRoutes(any()) } returns listOf(
            HikingRoute("1", "Írott-kő - Budapest - Hollóháza", SymbolType.Z),
            HikingRoute("2", "Országos Kéktúra 19. - Becske–Mátraverebély", SymbolType.K)
        )

        launch<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
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
        answerTestPlaceDetailsWay(landscape.osmId)
        coEvery { placesRepository.getHikingRoutes(any()) } returns listOf(
            HikingRoute("1", "Írott-kő - Budapest - Hollóháza", SymbolType.Z),
            HikingRoute("2", "Országos Kéktúra 19. - Becske–Mátraverebély", SymbolType.K)
        )
        coEvery { placesRepository.getPlaceDetails("1", PlaceType.RELATION) } returns PlaceDetails(
            osmId = "1",
            payload = Payload.Relation(
                ways = listOf(
                    Payload.Way(
                        osmId = "1",
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

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
            R.id.hikingRoutesList.isNotDisplayed()
        }
    }

    private fun answerTestPlaceDetailsWay(id: String) {
        coEvery { placesRepository.getPlaceDetails(id, any()) } returns PlaceDetails(
            osmId = id,
            payload = Payload.Way(
                osmId = id,
                locations = listOf(Location(47.123, 19.124)),
                distance = 5000
            )
        )
    }

    private fun answerTestHikingLayer() {
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

}

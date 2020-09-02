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
import hu.mostoha.mobile.android.turistautak.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.turistautak.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.repository.HikingLayerRepository
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import hu.mostoha.mobile.android.turistautak.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.turistautak.repository.PlacesRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.*
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class HomeActivityTest {

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
    fun givenNullLayerFile_whenDialogCanceled_thenMapShouldShown() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            R.string.download_layer_dialog_title.isTextDisplayed()

            R.string.download_layer_dialog_negative_button.clickWithText()

            R.id.homeMapView.isDisplayed()
        }
    }

    @Test
    fun givenTuraReteg1000_whenHomeOpens_thenHikingLayerDisplays() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            R.id.homeMapView.hasOverlayInPosition<TilesOverlay>(0)
        }
    }

    @Test
    fun whenMyLocationClicked_thenMyLocationOverlayDisplays() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            R.id.homeMyLocationButton.click()

            R.id.homeMapView.hasOverlayInPosition<MyLocationOverlay>(1)
        }
    }

    @Test
    fun givenSearchText_whenTyping_thenPlacePredictionsDisplay() {
        answerTestHikingLayer()
        answerTestPlacePredictions()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            "Mecseki Kéktúra".isPopupTextDisplayed()
            "Mecseknádasdi Piroska".isPopupTextDisplayed()
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetailsNode()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.placeDetailsContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()

            R.id.placeDetailsContainer.isDisplayed()
            "Mecseki Kéktúra".isTextDisplayed()
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplayOnMap() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetailsNode()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Marker>(1)
        }
    }

    @Test
    fun givenPlaceDetails_whenCloseClick_thenBottomSheetHidden() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetailsNode()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            R.id.placeDetailsContainer.isNotDisplayed()
        }
    }

    @Test
    fun givenPlaceDetails_whenCloseClick_thenMarkerRemoved() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetailsNode()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Marker>(1)

            R.id.placeDetailsCloseButton.click()

            R.id.homeMapView.hasNotOverlayInPosition<Marker>(1)
        }
    }

    @Test
    fun givenOpenWay_whenGetPlaceDetails_thenPolylineDisplays() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Kéktúra", null)
        )
        coEvery { placeRepository.getPlaceDetails(any(), any()) } returns PlaceDetails(
            id = "1",
            payLoad = PayLoad.Way(
                id = "1",
                locations = listOf(
                    Location(47.123, 19.124),
                    Location(47.125, 19.126),
                    Location(47.127, 19.128)
                ),
                distance = 5000
            )
        )

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polyline>(1)
        }
    }

    @Test
    fun givenClosedWay_whenGetPlaceDetails_thenPolygonDisplays() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Kéktúra", null)
        )
        coEvery { placeRepository.getPlaceDetails(any(), any()) } returns PlaceDetails(
            id = "1",
            payLoad = PayLoad.Way(
                id = "1",
                locations = listOf(
                    Location(47.123, 19.124),
                    Location(47.125, 19.126),
                    Location(47.123, 19.124)
                ),
                distance = 5000
            )
        )

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polygon>(1)
        }
    }

    @Test
    fun whenClickOnLandscape_thenPlaceDetailsDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestPlaceDetailsNode()

        launch<HomeActivity> {
            R.id.placeDetailsContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscapes.first().name.clickWithText()

            R.id.placeDetailsContainer.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoutes_whenClickHikingTrails_thenHikingRoutesDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscapes.first().id)
        coEvery { placeRepository.getHikingRoutes(any()) } returns listOf(
            HikingRoute("1", "Írott-kő - Budapest - Hollóháza", SymbolType.Z),
            HikingRoute("2", "Országos Kéktúra 19. - Becske–Mátraverebély", SymbolType.K)
        )

        launch<HomeActivity> {
            R.id.placeDetailsContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscapes.first().name.clickWithText()
            R.id.placeDetailsHikingTrailsButton.click()

            R.id.hikingRoutesList.isDisplayed()
        }
    }

    @Test
    fun givenHikingRoute_whenClick_thenHikingRouteDetailsDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestPlaceDetailsWay(landscapes.first().id)
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
            landscapes.first().name.clickWithText()
            R.id.placeDetailsHikingTrailsButton.click()

            "Írott-kő - Budapest - Hollóháza".clickWithText()

            R.id.placeDetailsContainer.isDisplayed()
            R.id.hikingRoutesList.isNotDisplayed()
        }
    }

    private fun answerTestPlacePredictions() {
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Kéktúra", null),
            PlacePrediction("2", PlaceType.NODE, "Mecseknádasdi Piroska", "Mecseknádasd")
        )
    }

    private fun answerTestPlaceDetailsNode() {
        coEvery { placeRepository.getPlaceDetails(any(), any()) } returns PlaceDetails(
            "1", PayLoad.Node(Location(47.123, 19.123))
        )
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
        val file = osmConfiguration.getHikingLayerFile().also {
            it.copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
        coEvery { layerRepository.getHikingLayerFile() } returns file
    }

}


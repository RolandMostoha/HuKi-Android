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
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.OverlayPositions
import hu.mostoha.mobile.android.huki.util.espresso.*
import hu.mostoha.mobile.android.huki.util.launch
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class MapBasicsUseCaseTest {

    companion object {
        private val HUNGARY_BOUNDING_BOX_CENTER = GeoPoint(47.31885723983627, 19.45407265979361)
        private const val HUNGARY_BOUNDING_BOX_ZOOM = 7.035469547173922
    }

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
    fun givenTuraReteg1000_whenMapOpens_thenHikingLayerDisplays() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            R.id.homeMapView.hasOverlayInPosition<TilesOverlay>(OverlayPositions.HIKING_LAYER)
        }
    }

    @Test
    fun whenMapOpens_thenItIsCenteredAndZoomedToHungary() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            R.id.homeMapView.hasCenterAndZoom(
                center = HUNGARY_BOUNDING_BOX_CENTER,
                zoom = HUNGARY_BOUNDING_BOX_ZOOM
            )
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Way".clickWithTextInPopup()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
            "Mecseki Way".isTextDisplayed()
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplayOnMap() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Node".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Marker>(OverlayPositions.PLACE)
        }
    }

    @Test
    fun givenPlaceDetails_whenCloseClick_thenBottomSheetIsHidden() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Way".clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
        }
    }

    @Test
    fun givenPlaceDetails_whenCloseClick_thenMarkerRemoved() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Way".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polyline>(OverlayPositions.PLACE)

            R.id.placeDetailsCloseButton.click()

            R.id.homeMapView.hasNotOverlayInPosition<Polyline>(OverlayPositions.PLACE)
        }
    }

    @Test
    fun givenOpenWay_whenGetPlaceDetails_thenPolylineDisplays() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Way", null)
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
            "Mecseki Way".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polyline>(OverlayPositions.PLACE)
        }
    }

    @Test
    fun givenClosedWay_whenGetPlaceDetails_thenPolygonDisplays() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Way", null)
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
            "Mecseki Way".clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polygon>(OverlayPositions.PLACE)
        }
    }

    private fun answerTestPlacePredictions() {
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Way", null),
            PlacePrediction("2", PlaceType.NODE, "Mecseki Node", "Mecseknádasd")
        )
    }

    private fun answerTestPlaceDetails() {
        coEvery { placeRepository.getPlaceDetails("1", any()) } returns PlaceDetails(
            "1", PayLoad.Way(
                "1", listOf(
                    Location(47.4979, 19.0402),
                    Location(47.4566, 19.0640)
                ), 100
            )
        )
        coEvery { placeRepository.getPlaceDetails("2", any()) } returns PlaceDetails(
            "2", PayLoad.Node(Location(47.123, 19.123))
        )
    }

    private fun answerTestHikingLayer() {
        val file = osmConfiguration.getHikingLayerFile().also {
            it.copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
        coEvery { layerRepository.getHikingLayerFile() } returns file
    }

}

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
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class MapPlacesUseCaseTest {

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
    fun givenTuraReteg1000_whenMapOpens_thenHikingLayerDisplays() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            R.id.homeMapView.hasOverlayInPosition<TilesOverlay>(OverlayPositions.HIKING_LAYER)
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplayOnBottomSheet() {
        answerTestHikingLayer()
        answerTestPlaces()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
            DEFAULT_PLACE_WAY.name.isTextDisplayed()
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplayOnMap() {
        answerTestHikingLayer()
        answerTestPlaces()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_NODE.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Marker>(OverlayPositions.PLACE)
        }
    }

    @Test
    fun givenPlaceDetails_whenCloseClick_thenBottomSheetIsHidden() {
        answerTestHikingLayer()
        answerTestPlaces()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.placeDetailsCloseButton.click()

            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
        }
    }

    @Test
    fun givenPlaceDetails_whenCloseClick_thenMarkerRemoved() {
        answerTestHikingLayer()
        answerTestPlaces()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polyline>(OverlayPositions.PLACE)

            R.id.placeDetailsCloseButton.click()

            R.id.homeMapView.hasNotOverlayInPosition<Polyline>(OverlayPositions.PLACE)
        }
    }

    @Test
    fun givenOpenWay_whenGetPlaceDetails_thenPolylineDisplays() {
        answerTestHikingLayer()
        coEvery { placesRepository.getPlacesBy(any()) } returns listOf(DEFAULT_PLACE_WAY)
        coEvery { placesRepository.getPlaceDetails(any(), any()) } returns (DEFAULT_PLACE_DETAILS_WAY)

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polyline>(OverlayPositions.PLACE)
        }
    }

    @Test
    fun givenClosedWay_whenGetPlaceDetails_thenPolygonDisplays() {
        answerTestHikingLayer()
        coEvery { placesRepository.getPlacesBy(any()) } returns listOf(DEFAULT_PLACE_WAY)
        coEvery { placesRepository.getPlaceDetails(any(), any()) } returns DEFAULT_PLACE_DETAILS_WAY.copy(
            payload = (DEFAULT_PLACE_DETAILS_WAY.payload as Payload.Way).copy(
                locations = listOf(
                    Location(47.123, 19.124),
                    Location(47.124, 19.125),
                    Location(47.123, 19.124)
                )
            )
        )

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlayInPosition<Polygon>(OverlayPositions.PLACE)
        }
    }

    private fun answerTestPlaces() {
        coEvery { placesRepository.getPlacesBy(any()) } returns listOf(
            DEFAULT_PLACE_WAY,
            DEFAULT_PLACE_NODE
        )
    }

    private fun answerTestPlaceDetails() {
        coEvery {
            placesRepository.getPlaceDetails(DEFAULT_PLACE_DETAILS_WAY.osmId, any())
        } returns DEFAULT_PLACE_DETAILS_WAY
        coEvery {
            placesRepository.getPlaceDetails(DEFAULT_PLACE_DETAILS_NODE.osmId, any())
        } returns DEFAULT_PLACE_DETAILS_NODE
    }

    private fun answerTestHikingLayer() {
        val file = osmConfiguration.getHikingLayerFile().also {
            it.copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns file
    }

    companion object {
        private val DEFAULT_PLACE_WAY = Place(
            osmId = "1",
            name = "Mecseki Kéktúra",
            placeType = PlaceType.WAY,
            location = Location(47.0983397, 17.7575106)
        )
        private val DEFAULT_PLACE_DETAILS_WAY = PlaceDetails(
            osmId = DEFAULT_PLACE_WAY.osmId,
            payload = Payload.Way(
                osmId = DEFAULT_PLACE_WAY.osmId,
                locations = listOf(
                    Location(47.123, 19.124),
                    Location(47.124, 19.126),
                    Location(47.125, 19.127)
                ),
                distance = 100
            )
        )
        private val DEFAULT_PLACE_NODE = Place(
            osmId = "2",
            name = "Mecsek hegy",
            placeType = PlaceType.NODE,
            location = Location(47.0982297, 17.7578106)
        )
        private val DEFAULT_PLACE_DETAILS_NODE = PlaceDetails(
            osmId = DEFAULT_PLACE_NODE.osmId,
            payload = Payload.Node(DEFAULT_PLACE_NODE.location)
        )
    }

}

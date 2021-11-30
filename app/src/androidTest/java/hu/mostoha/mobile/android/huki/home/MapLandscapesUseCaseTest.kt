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
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.ui.home.OverlayPositions
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlayInPosition
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
import org.osmdroid.views.overlay.Polygon
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
    fun givenLandscapes_whenClickOnLandscape_thenPlaceDetailsDisplayOnBottomSheet() {
        val landscape = landscapes.first { it.name == DEFAULT_LANDSCAPE_NAME }
        answerTestHikingLayer()
        answerTestWayGeometry(landscape.osmId)

        launch<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenLandscapes_whenClickOnLandscape_thenPolygonDisplaysOnMap() {
        val landscape = landscapes.first { it.name == DEFAULT_LANDSCAPE_NAME }
        answerTestHikingLayer()
        answerTestWayGeometry(landscape.osmId)

        launch<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()

            R.id.homeMapView.hasOverlayInPosition<Polygon>(OverlayPositions.PLACE)
        }
    }

    private fun answerTestWayGeometry(id: String) {
        coEvery { placesRepository.getGeometry(id, any()) } returns DEFAULT_GEOMETRY_LANDSCAPE
    }

    private fun answerTestHikingLayer() {
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

    companion object {
        private val DEFAULT_LANDSCAPE = Place(
            osmId = DEFAULT_LANDSCAPE_OSM_ID,
            name = DEFAULT_LANDSCAPE_NAME,
            placeType = PlaceType.RELATION,
            location = Location(DEFAULT_LANDSCAPE_LATITUDE, DEFAULT_LANDSCAPE_LONGITUDE)
        )
        private val DEFAULT_GEOMETRY_LANDSCAPE = Geometry.Way(
            osmId = DEFAULT_LANDSCAPE.osmId,
            locations = DEFAULT_LANDSCAPE_WAY_GEOMETRY.map { Location(it.first, it.second) },
            distance = 30_000
        )
    }

}

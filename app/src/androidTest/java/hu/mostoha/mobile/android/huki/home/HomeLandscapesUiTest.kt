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
import hu.mostoha.mobile.android.huki.data.localLandscapes
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.FileBasedHikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.ui.home.OverlayComparator
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isTextDisplayed
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.Polygon
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
class HomeLandscapesUiTest {

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
    val hikingLayerRepository: HikingLayerRepository = FileBasedHikingLayerRepository(testAppContext)

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

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
        val landscape = DEFAULT_CLOSE_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()

            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenLandscapes_whenClickOnLandscape_thenPolygonDisplaysOnMap() {
        val landscape = DEFAULT_CLOSE_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> {
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.clickWithText()

            R.id.homeMapView.hasOverlay<Polygon>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun whenRecreate_thenLandscapesAreDisplayedAgain() {
        val landscape = DEFAULT_CLOSE_LANDSCAPE
        answerTestLocationProvider()
        answerTestWayGeometry(landscape.osmId)

        launchScenario<HomeActivity> { scenario ->
            R.id.homePlaceDetailsBottomSheetContainer.isNotDisplayed()
            R.id.homeLandscapeChipGroup.isDisplayed()

            landscape.name.isTextDisplayed()

            scenario.recreate()

            landscape.name.isTextDisplayed()
        }
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

    private fun answerTestWayGeometry(id: String) {
        coEvery { placesRepository.getGeometry(id, any()) } returns DEFAULT_GEOMETRY_LANDSCAPE
    }

    companion object {
        private val DEFAULT_MY_LOCATION = Location(
            DEFAULT_MY_LOCATION_LATITUDE,
            DEFAULT_MY_LOCATION_LONGITUDE,
            DEFAULT_MY_LOCATION_ALTITUDE
        )
        private val DEFAULT_CLOSE_LANDSCAPE = localLandscapes
            .minByOrNull { DEFAULT_MY_LOCATION.distanceBetween(it.center) }!!
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

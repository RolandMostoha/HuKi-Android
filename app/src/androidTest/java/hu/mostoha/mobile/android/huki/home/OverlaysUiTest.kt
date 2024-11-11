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
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.VersionConfigurationModule
import hu.mostoha.mobile.android.huki.fake.FakeVersionConfiguration
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolygon
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.HikingRoutes
import hu.mostoha.mobile.android.huki.testdata.Landscapes
import hu.mostoha.mobile.android.huki.testdata.Landscapes.DEFAULT_LANDSCAPE
import hu.mostoha.mobile.android.huki.testdata.Places
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_NODE
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_PLACE_WAY
import hu.mostoha.mobile.android.huki.testdata.Places.DEFAULT_SEARCH_TEXT
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.clickWithScroll
import hu.mostoha.mobile.android.huki.util.espresso.clickWithText
import hu.mostoha.mobile.android.huki.util.espresso.clickWithTextInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasNoOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlay
import hu.mostoha.mobile.android.huki.util.espresso.hasOverlaysInOrder
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isNotDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(
    RepositoryModule::class,
    LocationModule::class,
    VersionConfigurationModule::class
)
class OverlaysUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @BindValue
    @JvmField
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @BindValue
    @JvmField
    val asyncMyLocationProvider: AsyncMyLocationProvider = mockk(relaxed = true)

    @BindValue
    @JvmField
    val layersRepository: LayersRepository = FileBasedLayersRepository(
        testAppContext,
        UnconfinedTestDispatcher(),
        LayersDomainModelMapper(),
        HukiGpxConfiguration(testAppContext),
        FakeExceptionLogger(),
    )

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val geocodingRepository: GeocodingRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()

        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

    @Test
    fun givenLandscapeDetails_whenClickOnPlace_thenLandscapeDetailsHides() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            landscape.nameRes.clickWithText()
            R.id.homeMapView.hasOverlay<LandscapePolygon>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            val searchText = DEFAULT_SEARCH_TEXT
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasNoOverlay<LandscapePolygon>()
            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
            R.id.homeHikeRecommenderBottomSheetContainer.isNotDisplayed()
            R.id.homePlaceDetailsBottomSheetContainer.isDisplayed()
        }
    }

    @Test
    fun givenPlaceDetails_whenClickOnLandscape_thenPlaceDetailsHides() {
        val landscape = DEFAULT_LANDSCAPE
        answerTestLocationProvider()
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()
            R.id.placeDetailsShowAllPointsButton.clickWithScroll()
            R.id.homeMapView.hasOverlay<Polyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            landscape.nameRes.clickWithText()

            R.id.homeMapView.hasNoOverlay<Polyline>()
            R.id.homeMapView.hasOverlay<LandscapePolygon>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    @Test
    fun givenPlaceDetails_whenClickOnHikingRoutes_thenPlaceDetailsHides() {
        answerTestLocationProvider()
        answerTestPlaces()
        answerTestGeometries()

        launchScenario<HomeActivity> {
            val searchText = DEFAULT_SEARCH_TEXT
            R.id.homeSearchBarInput.typeText(searchText)
            DEFAULT_PLACE_WAY.name.clickWithTextInPopup()

            R.id.homeMapView.hasOverlay<Marker>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)

            R.id.placeDetailsShowAllPointsButton.clickWithScroll()

            R.id.homeMapView.hasNoOverlay<Marker>()
            R.id.homeMapView.hasOverlay<Polyline>()
            R.id.homeMapView.hasOverlaysInOrder(OverlayComparator)
        }
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
    }

    private fun answerTestPlaces() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_WAY
        )
    }

    private fun answerTestGeometries() {
        coEvery {
            placesRepository.getGeometry(DEFAULT_LANDSCAPE.osmId, any())
        } returns Landscapes.DEFAULT_GEOMETRY_LANDSCAPE
        coEvery { placesRepository.getGeometry(DEFAULT_PLACE_NODE.osmId, any()) } returns Places.DEFAULT_GEOMETRY_NODE
        coEvery { placesRepository.getGeometry(DEFAULT_PLACE_WAY.osmId, any()) } returns Places.DEFAULT_GEOMETRY_WAY
        coEvery { placesRepository.getHikingRoutes(any()) } returns listOf(HikingRoutes.DEFAULT_HIKING_ROUTE)
    }

}

package hu.mostoha.mobile.android.huki.home

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
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
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_ADDRESS
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_CENTER_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_CENTER_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.BUDAPEST_LOCATION
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.espresso.click
import hu.mostoha.mobile.android.huki.util.espresso.clickImeActionButton
import hu.mostoha.mobile.android.huki.util.espresso.clickInPopup
import hu.mostoha.mobile.android.huki.util.espresso.hasFocus
import hu.mostoha.mobile.android.huki.util.espresso.hasNoFocus
import hu.mostoha.mobile.android.huki.util.espresso.isDisplayedWithText
import hu.mostoha.mobile.android.huki.util.espresso.isKeyboardShown
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.isSnackbarMessageDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.espresso.waitForInputFocusGain
import hu.mostoha.mobile.android.huki.util.flowOfError
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
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
class PlaceFinderUiTest {

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
    val versionConfiguration: VersionConfiguration = FakeVersionConfiguration()

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
    val placeHistoryRepository: PlaceHistoryRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()

        coEvery { placeHistoryRepository.clearOldPlaces() } returns Unit
        coEvery { placeHistoryRepository.getPlaces() } returns flowOf(emptyList())
        coEvery { placeHistoryRepository.getPlacesBy(any(), any()) } returns flowOf(emptyList())

        answerTestLocationProvider()
    }

    @Test
    fun whenSearchInputClicked_thenStaticActionsDisplays() {
        answerTestPlaces()

        launchScenario<HomeActivity> {
            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()

            R.string.place_finder_my_location_button.isPopupTextDisplayed()
            R.string.place_finder_pick_location_button.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenPlaces_whenTypingInSearchBar_thenPlacesSearchResultDisplays() {
        answerTestPlaces()

        launchScenario<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            DEFAULT_PLACE_NODE.name.isPopupTextDisplayed()
            DEFAULT_PLACE_WAY.name.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenPlaceFinderOpen_whenClickInMap_thenInputIsCleared() {
        answerTestPlaces()

        launchScenario<HomeActivity> {
            assertThat(isKeyboardShown()).isFalse()
            R.id.homeSearchBarInput.hasNoFocus()

            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()

            assertThat(isKeyboardShown()).isTrue()
            R.id.homeSearchBarInput.hasFocus()

            R.id.homeMapView.click()

            R.id.homeSearchBarInput.hasNoFocus()
        }
    }

    @Test
    fun givenSearchText_whenClickDoneImeAction_thenInputIsCleared() {
        answerTestPlaces()

        launchScenario<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            waitForInputFocusGain()

            DEFAULT_PLACE_NODE.name.isPopupTextDisplayed()
            DEFAULT_PLACE_WAY.name.isPopupTextDisplayed()

            R.id.homeSearchBarInput.clickImeActionButton()

            R.id.homeSearchBarInput.isDisplayedWithText("")
        }
    }

    @Test
    fun givenPlacesWithLocation_whenTypingInSearchBar_thenPlacesSearchResultDisplaysWithDistance() {
        answerTestPlaces()

        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns BUDAPEST_LOCATION.toMockLocation()

        launchScenario<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            waitForInputFocusGain()

            val nodeDistance = DEFAULT_PLACE_NODE.location.distanceBetween(BUDAPEST_LOCATION)
            val wayDistance = DEFAULT_PLACE_WAY.location.distanceBetween(BUDAPEST_LOCATION)

            DistanceFormatter.formatWithoutScale(nodeDistance)
                .resolve(testAppContext)
                .isPopupTextDisplayed()
            DistanceFormatter.formatWithoutScale(wayDistance)
                .resolve(testAppContext)
                .isPopupTextDisplayed()
        }
    }

    @Test
    fun givenNullMyLocation_whenClickMyLocationButton_thenSnackbarErrorMessageDisplays() {
        answerTestPlaces()
        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns null

        launchScenario<HomeActivity> {
            val searchText = " "

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.place_finder_my_location_button.isPopupTextDisplayed()
            R.string.place_finder_pick_location_button.isPopupTextDisplayed()

            R.id.placeFinderMyLocationButton.clickInPopup()

            R.string.place_finder_my_location_error_null_location.isSnackbarMessageDisplayed()
        }
    }

    @Test
    fun givenEmptyResult_whenTyping_thenEmptyViewDisplays() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns emptyList()

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.place_finder_empty_message.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenHistoryPlaces_whenTyping_thenEmptyViewDisplays() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns emptyList()
        coEvery { placeHistoryRepository.getPlaces() } returns flowOf(listOf(DEFAULT_PLACE_HISTORY))
        coEvery { placeHistoryRepository.getPlacesBy(any(), any()) } returns flowOf(listOf(DEFAULT_PLACE_HISTORY))

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()

            DEFAULT_PLACE_HISTORY.name.isPopupTextDisplayed()

            R.id.homeSearchBarInput.typeText(searchText)

            DEFAULT_PLACE_HISTORY.name.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenErrorOnHistoryPlaces_whenTyping_thenErrorViewDisplays() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns emptyList()
        coEvery { placeHistoryRepository.getPlacesBy(any(), any()) } returns flowOf(listOf(DEFAULT_PLACE_HISTORY))
        every { placeHistoryRepository.getPlaces() } returns flowOfError(IllegalStateException("Error"))

        launchScenario<HomeActivity> {
            R.id.homeSearchBarInput.typeText("A")
            waitForInputFocusGain()

            R.string.error_message_unknown.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenIllegalStateException_whenTyping_thenErrorViewDisplaysWithMessageAndDetailsButton() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } throws IllegalStateException("Error")

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.error_message_unknown.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenTooManyRequestsException_whenTyping_thenErrorViewDisplaysWithMessageOnly() {
        coEvery {
            geocodingRepository.getPlacesBy(any(), any(), any())
        } throws HttpException(Response.error<Unit>(429, "".toResponseBody()))

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.error_message_too_many_requests.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenPlaces_whenRecreate_thenPlacesSearchResultDisplaysAgain() {
        answerTestPlaces()

        launchScenario<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            DEFAULT_PLACE_NODE.name.isPopupTextDisplayed()
            DEFAULT_PLACE_WAY.name.isPopupTextDisplayed()

            recreate()

            R.string.place_finder_my_location_button.isPopupTextDisplayed()
            R.string.place_finder_pick_location_button.isPopupTextDisplayed()
        }
    }

    private fun answerTestPlaces() {
        coEvery { geocodingRepository.getPlacesBy(any(), any(), any()) } returns listOf(
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_WAY
        )
    }

    private fun answerTestLocationProvider() {
        every { asyncMyLocationProvider.startLocationProvider(any()) } returns true
        every { asyncMyLocationProvider.getLocationFlow() } returns flowOf(DEFAULT_MY_LOCATION.toMockLocation())
        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns DEFAULT_MY_LOCATION.toMockLocation()
    }

    companion object {
        private val DEFAULT_PLACE_NODE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            name = DEFAULT_NODE_NAME,
            address = DEFAULT_NODE_CITY,
            placeFeature = PlaceFeature.MAP_SEARCH,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            placeType = PlaceType.WAY,
            name = DEFAULT_WAY_NAME,
            address = DEFAULT_WAY_CITY,
            placeFeature = PlaceFeature.ROUTE_PLANNER_SEARCH,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE)
        )
        private val DEFAULT_PLACE_HISTORY = Place(
            osmId = DEFAULT_RELATION_OSM_ID,
            placeType = PlaceType.RELATION,
            name = DEFAULT_RELATION_NAME,
            address = DEFAULT_RELATION_ADDRESS,
            placeFeature = PlaceFeature.ROUTE_PLANNER_SEARCH,
            location = Location(DEFAULT_RELATION_CENTER_LATITUDE, DEFAULT_RELATION_CENTER_LONGITUDE)
        )
    }

}

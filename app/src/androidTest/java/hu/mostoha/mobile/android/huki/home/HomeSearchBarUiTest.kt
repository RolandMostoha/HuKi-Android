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
import hu.mostoha.mobile.android.huki.di.module.LocationModule
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.extensions.resolve
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
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.ui.util.DistanceFormatter
import hu.mostoha.mobile.android.huki.util.BUDAPEST_LOCATION
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, LocationModule::class)
class HomeSearchBarUiTest {

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

        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns null
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
    fun givenPlacesWithLocation_whenTypingInSearchBar_thenPlacesSearchResultDisplaysWithDistance() {
        answerTestPlaces()

        coEvery { asyncMyLocationProvider.getLastKnownLocationCoroutine() } returns BUDAPEST_LOCATION.toMockLocation()

        launchScenario<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            val nodeDistance = DEFAULT_PLACE_NODE.location.distanceBetween(BUDAPEST_LOCATION)
            val wayDistance = DEFAULT_PLACE_WAY.location.distanceBetween(BUDAPEST_LOCATION)

            DistanceFormatter.format(nodeDistance)
                .resolve(testAppContext)
                .isPopupTextDisplayed()
            DistanceFormatter.format(wayDistance)
                .resolve(testAppContext)
                .isPopupTextDisplayed()
        }
    }

    @Test
    fun givenEmptyResult_whenTyping_thenEmptyViewDisplays() {
        coEvery { placesRepository.getPlacesBy(any(), any()) } returns emptyList()

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.search_bar_empty_message.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenIllegalStateException_whenTyping_thenErrorViewDisplaysWithMessageAndDetailsButton() {
        coEvery { placesRepository.getPlacesBy(any(), any()) } throws IllegalStateException("Error")

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.error_message_unknown.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenTooManyRequestsException_whenTyping_thenErrorViewDisplaysWithMessageOnly() {
        coEvery {
            placesRepository.getPlacesBy(any(), any())
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

        launchScenario<HomeActivity> { scenario ->
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            DEFAULT_PLACE_NODE.name.isPopupTextDisplayed()
            DEFAULT_PLACE_WAY.name.isPopupTextDisplayed()

            scenario.recreate()

            DEFAULT_PLACE_NODE.name.isPopupTextDisplayed()
            DEFAULT_PLACE_WAY.name.isPopupTextDisplayed()
        }
    }

    private fun answerTestPlaces() {
        coEvery { placesRepository.getPlacesBy(any(), any()) } returns listOf(
            DEFAULT_PLACE_NODE,
            DEFAULT_PLACE_WAY
        )
    }

    companion object {
        private val DEFAULT_PLACE_NODE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            name = DEFAULT_WAY_NAME,
            placeType = PlaceType.WAY,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE)
        )
    }

}

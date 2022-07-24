package hu.mostoha.mobile.android.huki.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.*
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launchScenario
import hu.mostoha.mobile.android.huki.util.testAppContext
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
@UninstallModules(RepositoryModule::class)
class HomeSearchBarUiTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

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
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
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
    fun givenEmptyResult_whenTyping_thenEmptyViewDisplays() {
        coEvery { placesRepository.getPlacesBy(any()) } returns emptyList()

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.search_bar_empty_message.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenIllegalStateException_whenTyping_thenErrorViewDisplaysWithMessageAndDetailsButton() {
        coEvery { placesRepository.getPlacesBy(any()) } throws IllegalStateException("Error")

        launchScenario<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.error_message_unknown.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenTooManyRequestsException_whenTyping_thenErrorViewDisplaysWithMessageOnly() {
        coEvery {
            placesRepository.getPlacesBy(any())
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
        coEvery { placesRepository.getPlacesBy(any()) } returns listOf(
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

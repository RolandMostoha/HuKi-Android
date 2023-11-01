package hu.mostoha.mobile.android.huki.ui.home.placefinder

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.configuration.TestAppConfiguration
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.mapper.PlaceFinderUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PlaceFinderViewModelTest {

    private lateinit var viewModel: PlaceFinderViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val myLocationProvider = mockk<AsyncMyLocationProvider>()
    private val geocodingRepository = mockk<GeocodingRepository>()
    private val placeFinderUiModelMapper = mockk<PlaceFinderUiModelMapper>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns null

        viewModel = PlaceFinderViewModel(
            TestAppConfiguration(),
            exceptionLogger,
            myLocationProvider,
            geocodingRepository,
            placeFinderUiModelMapper,
        )
    }

    @Test
    fun `Given place, when loadPlaces, then place finder items are emitted with loading`() =
        runTestDefault {
            val searchText = "Mecs"
            val places = listOf(DEFAULT_PLACE)
            val placeFinderItems = listOf(PlaceFinderItem.Place(DEFAULT_PLACE_UI_MODEL))
            coEvery { geocodingRepository.getPlacesBy(searchText) } returns places
            every { placeFinderUiModelMapper.mapPlaceFinderItems(places) } returns placeFinderItems

            viewModel.placeFinderItems.test {
                viewModel.loadPlaces(searchText)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.StaticActions, PlaceFinderItem.Loading))
                assertThat(awaitItem()).isEqualTo(placeFinderItems)
            }
        }

    @Test
    fun `Given my location, when initStaticActions, then static action item is emitted`() =
        runTestDefault {
            val myLocation = DEFAULT_MY_LOCATION
            coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns myLocation.toMockLocation()

            viewModel.placeFinderItems.test {
                viewModel.initStaticActions()

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.StaticActions))
            }
        }

    @Test
    fun `Given search text with location, when loadPlaces, then place finder items are emitted with loading`() =
        runTestDefault {
            val searchText = "Mecs"
            val places = listOf(DEFAULT_PLACE)
            val placeFinderItems = listOf(PlaceFinderItem.Place(DEFAULT_PLACE_UI_MODEL))
            val myLocation = DEFAULT_MY_LOCATION
            coEvery { geocodingRepository.getPlacesBy(searchText, myLocation) } returns places
            every { placeFinderUiModelMapper.mapPlaceFinderItems(places, myLocation) } returns placeFinderItems
            coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns myLocation.toMockLocation()

            viewModel.placeFinderItems.test {
                viewModel.loadPlaces(searchText)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.StaticActions, PlaceFinderItem.Loading))
                assertThat(awaitItem()).isEqualTo(placeFinderItems)
            }
        }

    @Test
    fun `When cancelSearch, then place finder items are cleared`() =
        runTestDefault {
            val searchText = "Mecs"
            val places = listOf(DEFAULT_PLACE)
            val placeFinderItems = listOf(PlaceFinderItem.Place(DEFAULT_PLACE_UI_MODEL))
            coEvery { geocodingRepository.getPlacesBy(searchText) } returns places
            every { placeFinderUiModelMapper.mapPlaceFinderItems(places) } returns placeFinderItems

            viewModel.placeFinderItems.test {
                viewModel.loadPlaces(searchText)
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.StaticActions, PlaceFinderItem.Loading))
                assertThat(awaitItem()).isEqualTo(placeFinderItems)

                viewModel.cancelSearch()
                assertThat(awaitItem()).isNull()
            }
        }

    companion object {

        private val DEFAULT_MY_LOCATION = Location(
            DEFAULT_MY_LOCATION_LATITUDE,
            DEFAULT_MY_LOCATION_LONGITUDE
        )
        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_PLACE.osmId,
            placeType = DEFAULT_PLACE.placeType,
            primaryText = DEFAULT_PLACE.name.toMessage(),
            secondaryText = null,
            iconRes = 0,
            geoPoint = DEFAULT_PLACE.location.toGeoPoint(),
            boundingBox = null,
        )
    }

}

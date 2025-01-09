package hu.mostoha.mobile.android.huki.ui.home.placefinder

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.TestAppConfiguration
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.mapper.HikingRouteRelationMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDomainUiMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceFinderUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_MY_LOCATION_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.TimberRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import hu.mostoha.mobile.android.huki.util.toMockLocation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PlaceFinderViewModelTest {

    private lateinit var viewModel: PlaceFinderViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val myLocationProvider = mockk<AsyncMyLocationProvider>()
    private val geocodingRepository = mockk<GeocodingRepository>()
    private val placeHistoryRepository = mockk<PlaceHistoryRepository>()
    private val hikingRouteRelationMapper = HikingRouteRelationMapper()
    private val placeMapper = PlaceDomainUiMapper(hikingRouteRelationMapper)
    private val placeFinderUiModelMapper = PlaceFinderUiModelMapper(placeMapper)

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns null

        viewModel = PlaceFinderViewModel(
            UnconfinedTestDispatcher(),
            TestAppConfiguration(),
            exceptionLogger,
            myLocationProvider,
            geocodingRepository,
            placeHistoryRepository,
            placeFinderUiModelMapper,
        )
    }

    @Test
    fun `Given empty places, when load places, then empty place finder item is emitted`() {
        runTestDefault {
            val searchText = "Mecs"
            val placeFeature = PlaceFeature.MAP_SEARCH
            coEvery { geocodingRepository.getPlacesBy(searchText, placeFeature, any()) } returns emptyList()
            coEvery { placeHistoryRepository.getPlaces() } returns flowOf(emptyList())
            coEvery { placeHistoryRepository.getPlacesBy(any(), any()) } returns flowOf(emptyList())

            viewModel.placeFinderItems.test {
                viewModel.initPlaceFinder(PlaceFinderFeature.MAP)
                advanceUntilIdle()
                viewModel.loadPlaces(searchText, placeFeature)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.StaticActions))
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        PlaceFinderItem.Info(
                            messageRes = R.string.place_finder_empty_message.toMessage(),
                            drawableRes = R.drawable.ic_search_bar_empty_result
                        )
                    )
                )
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.Loading))
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        PlaceFinderItem.Info(
                            messageRes = R.string.place_finder_empty_message.toMessage(),
                            drawableRes = R.drawable.ic_search_bar_empty_result
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given places, when load places, then place finder items are emitted`() {
        runTestDefault {
            val searchText = "Mecs"
            val places = listOf(DEFAULT_PLACE)
            val placeFeature = PlaceFeature.MAP_SEARCH
            coEvery { geocodingRepository.getPlacesBy(searchText, placeFeature, any()) } returns places
            coEvery { placeHistoryRepository.getPlaces() } returns flowOf(places)
            coEvery { placeHistoryRepository.getPlacesBy(any(), any()) } returns flowOf(places)

            viewModel.placeFinderItems.test {
                viewModel.initPlaceFinder(PlaceFinderFeature.MAP)
                advanceUntilIdle()
                viewModel.loadPlaces(searchText, placeFeature)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(PlaceFinderItem.StaticActions)
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places))
                )
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places))
                )
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places))
                        .plus(PlaceFinderItem.Loading)
                )
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places))
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places))
                )
            }
        }
    }

    @Test
    fun `Given places with location, when load places, then place finder items are emitted`() {
        runTestDefault {
            val searchText = "Mecs"
            val places = listOf(DEFAULT_PLACE)
            val myLocation = DEFAULT_MY_LOCATION
            val placeFeature = PlaceFeature.MAP_SEARCH
            coEvery { geocodingRepository.getPlacesBy(searchText, placeFeature, myLocation) } returns places
            coEvery { placeHistoryRepository.getPlaces() } returns flowOf(places)
            coEvery { placeHistoryRepository.getPlacesBy(any(), any()) } returns flowOf(places)
            coEvery { myLocationProvider.getLastKnownLocationCoroutine() } returns myLocation.toMockLocation()

            viewModel.placeFinderItems.test {
                viewModel.initPlaceFinder(PlaceFinderFeature.MAP)
                advanceUntilIdle()
                viewModel.loadPlaces(searchText, placeFeature)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(PlaceFinderItem.StaticActions)
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places, myLocation))
                )
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places, myLocation))
                )
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places, myLocation))
                        .plus(PlaceFinderItem.Loading)
                )
                assertThat(awaitItem()).isEqualTo(
                    emptyList<PlaceFinderItem>()
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places, myLocation))
                        .plus(placeFinderUiModelMapper.mapPlaceFinderItems(places, myLocation))
                )
            }
        }
    }

    @Test
    fun `When cancelSearch, then place finder items are cleared`() =
        runTestDefault {
            coEvery { placeHistoryRepository.getPlaces() } returns flowOf(emptyList())

            viewModel.placeFinderItems.test {
                viewModel.initPlaceFinder(PlaceFinderFeature.MAP)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(PlaceFinderItem.StaticActions))

                viewModel.cancelSearch()
                assertThat(awaitItem()).isNull()
            }
        }

    companion object {
        @get:ClassRule
        @JvmStatic
        var timberRule = TimberRule()

        private val DEFAULT_MY_LOCATION = Location(
            DEFAULT_MY_LOCATION_LATITUDE,
            DEFAULT_MY_LOCATION_LONGITUDE
        )
        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME.toMessage(),
            fullAddress = DEFAULT_NODE_CITY,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.MAP_SEARCH,
        )
    }

}

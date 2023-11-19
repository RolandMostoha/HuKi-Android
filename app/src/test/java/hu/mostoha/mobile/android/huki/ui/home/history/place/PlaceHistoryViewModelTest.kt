package hu.mostoha.mobile.android.huki.ui.home.history.place

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.mapper.HikingRouteRelationMapper
import hu.mostoha.mobile.android.huki.model.mapper.HistoryUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDomainUiMapper
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.TimberRule
import hu.mostoha.mobile.android.huki.util.answerDefaults
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PlaceHistoryViewModelTest {

    private lateinit var viewModel: PlaceHistoryViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val placeHistoryRepository = mockk<PlaceHistoryRepository>()
    private val dateTimeProvider = mockk<DateTimeProvider>()
    private val hikingRouteRelationMapper = HikingRouteRelationMapper()
    private val placeMapper = PlaceDomainUiMapper(hikingRouteRelationMapper)
    private val historyUiModelMapper = HistoryUiModelMapper(placeMapper)

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        dateTimeProvider.answerDefaults()
        every { placeHistoryRepository.getPlaces() } returns flowOf(listOf(DEFAULT_PLACE))
        coEvery { placeHistoryRepository.deletePlace(any()) } returns Unit

        viewModel = PlaceHistoryViewModel(
            exceptionLogger,
            placeHistoryRepository,
            historyUiModelMapper,
            dateTimeProvider
        )
    }

    @Test
    fun `When init, then get places is called in history repository`() {
        runTest {
            verify { placeHistoryRepository.getPlaces() }
        }
    }

    companion object {
        @get:ClassRule
        @JvmStatic
        var timberRule = TimberRule()

        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            address = DEFAULT_NODE_CITY,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.MAP_SEARCH,
        )
    }

}

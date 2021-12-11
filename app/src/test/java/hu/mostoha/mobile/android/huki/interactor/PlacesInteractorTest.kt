package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.executor.TestTaskExecutor
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PlacesInteractorTest {

    private lateinit var placesInteractor: PlacesInteractor

    private val placesRepository = mockk<PlacesRepository>()

    @Before
    fun setUp() {
        placesInteractor = PlacesInteractor(TestTaskExecutor(), placesRepository)
    }

    @Test
    fun `Given search text, when requestGetPlacesBy, then success task result returns`() {
        runBlockingTest {
            val searchText = "Mecsek"
            val places = listOf(DEFAULT_PLACE)
            coEvery { placesRepository.getPlacesBy(searchText) } returns places

            val taskResult = placesInteractor.requestGetPlacesBy(searchText)

            assertThat(taskResult).isEqualTo(TaskResult.Success(places))
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetPlacesBy, then error task result returns with mapped exception`() {
        runBlockingTest {
            val searchText = "Mecsek"
            val exception = IllegalStateException("Unknown exception")
            coEvery { placesRepository.getPlacesBy(searchText) } throws exception

            val taskResult = placesInteractor.requestGetPlacesBy(searchText)

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(GeneralDomainExceptionMapper.map(exception))
            )
        }
    }

    @Test
    fun `Given osmId and placeType, when requestGetGeometry, then success task result returns`() {
        runBlockingTest {
            val osmId = DEFAULT_PLACE.osmId
            val placeType = DEFAULT_PLACE.placeType
            coEvery { placesRepository.getGeometry(osmId, placeType) } returns DEFAULT_GEOMETRY

            val taskResult = placesInteractor.requestGetGeometry(osmId, placeType)

            assertThat(taskResult).isEqualTo(TaskResult.Success(DEFAULT_GEOMETRY))
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetGeometry, then error task result returns with mapped exception`() {
        runBlockingTest {
            val osmId = DEFAULT_PLACE.osmId
            val placeType = DEFAULT_PLACE.placeType
            val exception = IllegalStateException("Unknown exception")
            coEvery { placesRepository.getGeometry(osmId, placeType) } throws exception

            val taskResult = placesInteractor.requestGetGeometry(osmId, placeType)

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(GeneralDomainExceptionMapper.map(exception))
            )
        }
    }

    @Test
    fun `Given bounding box, when requestGetHikingRoutes, then success task result returns`() {
        runBlockingTest {
            val boundingBox = DEFAULT_BOUNDING_BOX
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            coEvery { placesRepository.getHikingRoutes(boundingBox) } returns hikingRoutes

            val taskResult = placesInteractor.requestGetHikingRoutes(boundingBox)

            assertThat(taskResult).isEqualTo(TaskResult.Success(hikingRoutes))
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetHikingRoutes, then error task result returns with mapped exception`() {
        runBlockingTest {
            val boundingBox = DEFAULT_BOUNDING_BOX
            val exception = IllegalStateException("Unknown exception")
            coEvery { placesRepository.getHikingRoutes(boundingBox) } throws exception

            val taskResult = placesInteractor.requestGetHikingRoutes(boundingBox)

            assertThat(taskResult).isEqualTo(
                TaskResult.Error(GeneralDomainExceptionMapper.map(exception))
            )
        }
    }

    companion object {
        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
        )
        private val DEFAULT_GEOMETRY = Geometry.Node(
            osmId = DEFAULT_PLACE.osmId,
            location = DEFAULT_PLACE.location
        )
        private val DEFAULT_BOUNDING_BOX = BoundingBox(
            north = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH,
            east = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST,
            south = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH,
            west = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
        )
        private val DEFAULT_HIKING_ROUTE = HikingRoute(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolType = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
        )
    }

}

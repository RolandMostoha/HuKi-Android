package hu.mostoha.mobile.android.huki.interactor

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_JEL
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.util.BUDAPEST_LOCATION
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PlacesInteractorTest {

    private lateinit var placesInteractor: PlacesInteractor

    private val placesRepository = mockk<PlacesRepository>()

    private val exceptionLogger = mockk<ExceptionLogger>()

    @Before
    fun setUp() {
        placesInteractor = PlacesInteractor(exceptionLogger, placesRepository)

        every { exceptionLogger.recordException(any()) } returns Unit
    }

    @Test
    fun `Given search text, when requestGetPlacesByFlow, then places are emitted`() {
        runTest {
            val searchText = "Mecsek"
            val places = listOf(DEFAULT_PLACE)
            coEvery { placesRepository.getPlacesBy(searchText) } returns places

            val flow = placesInteractor.requestGetPlacesByFlow(searchText)

            flow.test {
                assertThat(awaitItem()).isEqualTo(places)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given search text with location, when requestGetPlacesByFlow, then places are emitted`() {
        runTest {
            val searchText = "Mecsek"
            val places = listOf(DEFAULT_PLACE)
            val location = BUDAPEST_LOCATION
            coEvery { placesRepository.getPlacesBy(searchText, location) } returns places

            val flow = placesInteractor.requestGetPlacesByFlow(searchText, location)

            flow.test {
                assertThat(awaitItem()).isEqualTo(places)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetPlacesByFlow, then unknown domain exception is emitted`() {
        runTest {
            val searchText = "Mecsek"
            val exception = IllegalStateException("Unknown exception")
            coEvery { placesRepository.getPlacesBy(searchText) } throws exception

            val flow = placesInteractor.requestGetPlacesByFlow(searchText)

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    @Test
    fun `Given CancellationException, when requestGetPlacesByFlow, then cancellation exception is emitted`() {
        runTest {
            val searchText = "Mecsek"
            val exception = CancellationException("StandaloneCoroutine was cancelled")
            coEvery { placesRepository.getPlacesBy(searchText) } throws exception

            val flow = placesInteractor.requestGetPlacesByFlow(searchText)

            flow.test {
                assertThat(awaitError()).isEqualTo(JobCancellationException(exception))
            }
        }
    }

    @Test
    fun `Given osmId and placeType, when requestGeometryFlow, then geometry is emitted`() {
        runTest {
            val osmId = DEFAULT_PLACE.osmId
            val placeType = DEFAULT_PLACE.placeType
            coEvery { placesRepository.getGeometry(osmId, placeType) } returns DEFAULT_GEOMETRY

            val flow = placesInteractor.requestGeometryFlow(osmId, placeType)

            flow.test {
                assertThat(awaitItem()).isEqualTo(DEFAULT_GEOMETRY)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGeometryFlow, then unknown domain exception is emitted`() {
        runTest {
            val osmId = DEFAULT_PLACE.osmId
            val placeType = DEFAULT_PLACE.placeType
            val exception = IllegalStateException("Unknown exception")
            coEvery { placesRepository.getGeometry(osmId, placeType) } throws exception

            val flow = placesInteractor.requestGeometryFlow(osmId, placeType)

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
        }
    }

    @Test
    fun `Given bounding box, when requestGetHikingRoutesFlow, then hiking routes are emitted`() {
        runTest {
            val boundingBox = DEFAULT_BOUNDING_BOX
            val hikingRoutes = listOf(DEFAULT_HIKING_ROUTE)
            coEvery { placesRepository.getHikingRoutes(boundingBox) } returns hikingRoutes

            val flow = placesInteractor.requestGetHikingRoutesFlow(boundingBox)

            flow.test {
                assertThat(awaitItem()).isEqualTo(hikingRoutes)
                awaitComplete()
            }
        }
    }

    @Test
    fun `Given IllegalStateException, when requestGetHikingRoutesFlow, then unknown domain exception is emitted`() {
        runTest {
            val boundingBox = DEFAULT_BOUNDING_BOX
            val exception = IllegalStateException("Unknown exception")
            coEvery { placesRepository.getHikingRoutes(boundingBox) } throws exception

            val flow = placesInteractor.requestGetHikingRoutesFlow(boundingBox)

            flow.test {
                assertThat(awaitError()).isEqualTo(UnknownException(exception))
            }
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

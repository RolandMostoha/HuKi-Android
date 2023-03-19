package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_CLOSED
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_OPEN
import hu.mostoha.mobile.android.huki.util.calculateDecline
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.util.calculateIncline
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import io.ticofab.androidgpxparser.parser.domain.Gpx
import io.ticofab.androidgpxparser.parser.domain.Track
import io.ticofab.androidgpxparser.parser.domain.TrackPoint
import io.ticofab.androidgpxparser.parser.domain.TrackSegment
import org.junit.Assert.assertThrows
import org.junit.Test

class LayersDomainModelMapperTest {

    private val mapper = LayersDomainModelMapper()

    @Test
    fun `Given GPX with closed track points, when mapGpxDetails, then GPX Details returns`() {
        val fileName = "dera_szurdok.gpx"
        val gpx = DEFAULT_GPX_CLOSED
        val expectedLocations = DEFAULT_GPX_WAY_CLOSED.map { Location(it.first, it.second, it.third) }

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = expectedLocations,
                travelTime = expectedLocations.calculateTravelTime(),
                distance = expectedLocations.calculateDistance(),
                altitudeRange = Pair(
                    expectedLocations.mapNotNull { it.altitude }
                        .min()
                        .toInt(),
                    expectedLocations.mapNotNull { it.altitude }
                        .max()
                        .toInt()
                ),
                incline = expectedLocations.calculateIncline(),
                decline = expectedLocations.calculateDecline(),
                isClosed = true
            )
        )
    }

    @Test
    fun `Given GPX with open track points, when mapGpxDetails, then GPX Details returns`() {
        val fileName = "dera_szurdok_open.gpx"
        val expectedLocations = DEFAULT_GPX_WAY_OPEN.map { Location(it.first, it.second, it.third) }
        val gpx = DEFAULT_GPX_OPEN

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = expectedLocations,
                travelTime = expectedLocations.calculateTravelTime(),
                distance = expectedLocations.calculateDistance(),
                altitudeRange = Pair(
                    expectedLocations.mapNotNull { it.altitude }
                        .min()
                        .toInt(),
                    expectedLocations.mapNotNull { it.altitude }
                        .max()
                        .toInt()
                ),
                incline = expectedLocations.calculateIncline(),
                decline = expectedLocations.calculateDecline(),
                isClosed = false
            )
        )
    }

    @Test
    fun `Given GPX track points without altitude, when mapGpxDetails, then GPX Details returns`() {
        val fileName = "dera_szurdok_without_altitude.gpx"
        val expectedLocations = DEFAULT_GPX_WAY_OPEN.map { Location(it.first, it.second) }
        val gpx = DEFAULT_GPX_WITHOUT_ALTITUDE

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = expectedLocations,
                travelTime = expectedLocations.calculateTravelTime(),
                distance = expectedLocations.calculateDistance(),
                altitudeRange = Pair(0, 0),
                incline = expectedLocations.calculateIncline(),
                decline = expectedLocations.calculateDecline(),
                isClosed = false
            )
        )
    }

    @Test
    fun `Given GPX with empty track points, when mapGpxDetails, then GPX parse failed exception throws`() {
        val fileName = "dera_szurdok_empty.gpx"
        val gpx = Gpx.Builder()
            .setWayPoints(emptyList())
            .setRoutes(emptyList())
            .setTracks(emptyList())
            .build()

        val exception = assertThrows(GpxParseFailedException::class.java) {
            mapper.mapGpxDetails(fileName, gpx)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_gpx_parse_failed.toMessage())
    }

    companion object {
        private val DEFAULT_GPX_CLOSED = createGpx(
            DEFAULT_GPX_WAY_CLOSED.map {
                TrackPoint.Builder()
                    .setLatitude(it.first)
                    .setLongitude(it.second)
                    .setElevation(it.third)
                    .build() as TrackPoint
            }
        )
        private val DEFAULT_GPX_OPEN = createGpx(
            DEFAULT_GPX_WAY_OPEN.map {
                TrackPoint.Builder()
                    .setLatitude(it.first)
                    .setLongitude(it.second)
                    .setElevation(it.third)
                    .build() as TrackPoint
            }
        )
        private val DEFAULT_GPX_WITHOUT_ALTITUDE = createGpx(
            DEFAULT_GPX_WAY_OPEN.map {
                TrackPoint.Builder()
                    .setLatitude(it.first)
                    .setLongitude(it.second)
                    .build() as TrackPoint
            }
        )

        private fun createGpx(trackPoints: List<TrackPoint>): Gpx {
            return Gpx.Builder()
                .setWayPoints(emptyList())
                .setRoutes(emptyList())
                .setTracks(
                    listOf(
                        Track.Builder()
                            .setTrackSegments(
                                listOf(
                                    TrackSegment.Builder()
                                        .setTrackPoints(trackPoints)
                                        .build()
                                )
                            )
                            .build()
                    )
                )
                .build()
        }
    }

}

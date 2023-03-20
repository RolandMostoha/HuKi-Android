package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toGpxWaypoints
import hu.mostoha.mobile.android.huki.util.calculateDecline
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.util.calculateIncline
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import io.ticofab.androidgpxparser.parser.domain.Gpx
import javax.inject.Inject

class LayersDomainModelMapper @Inject constructor() {

    companion object {
        private const val GPX_GEO_POINTS_LIMIT = 2
    }

    fun mapGpxDetails(fileName: String, gpx: Gpx): GpxDetails {
        val locations = when {
            gpx.tracks.isNotEmpty() -> {
                gpx.tracks
                    .flatMap { it.trackSegments }
                    .flatMap { it.trackPoints }
                    .map { trackPoint ->
                        Location(trackPoint.latitude, trackPoint.longitude, trackPoint.elevation)
                    }
            }
            gpx.routes.isNotEmpty() -> {
                gpx.routes
                    .flatMap { it.routePoints }
                    .map { routePoint ->
                        Location(routePoint.latitude, routePoint.longitude, routePoint.elevation)
                    }
            }
            else -> throw GpxParseFailedException(IllegalArgumentException("Tracks and routes are empty"))
        }

        if (locations.isEmpty() || locations.size < GPX_GEO_POINTS_LIMIT) {
            throw GpxParseFailedException(IllegalArgumentException("Tracks and routes are empty"))
        }

        val gpxWaypoints = gpx.wayPoints.toGpxWaypoints()

        val minAltitude = locations
            .mapNotNull { it.altitude }
            .minOrNull() ?: 0.0

        val maxAltitude = locations
            .mapNotNull { it.altitude }
            .maxOrNull() ?: 0.0
        val distance = locations.calculateDistance()

        return GpxDetails(
            fileName = fileName,
            locations = locations,
            gpxWaypoints = gpxWaypoints,
            distance = distance,
            travelTime = locations.calculateTravelTime(),
            altitudeRange = minAltitude.toInt() to maxAltitude.toInt(),
            incline = locations.calculateIncline(),
            decline = locations.calculateDecline(),
        )
    }

}

package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.util.calculateDecline
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.util.calculateIncline
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import io.ticofab.androidgpxparser.parser.domain.Gpx
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import javax.inject.Inject

class LayersDomainModelMapper @Inject constructor() {

    fun mapGpxDetails(fileUri: String, fileName: String, gpx: Gpx): GpxDetails {
        if (gpx.tracks.isEmpty() && gpx.routes.isEmpty() && gpx.wayPoints.isEmpty()) {
            throw GpxParseFailedException(IllegalArgumentException("GPX must contain one track, route or waypoint"))
        }

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
            else -> emptyList()
        }

        val gpxWaypoints = mapGpxWaypoints(gpx.wayPoints)

        val minAltitude = locations
            .mapNotNull { it.altitude }
            .minOrNull() ?: 0.0
        val maxAltitude = locations
            .mapNotNull { it.altitude }
            .maxOrNull() ?: 0.0

        return GpxDetails(
            fileName = fileName,
            fileUri = fileUri,
            locations = locations,
            gpxWaypoints = gpxWaypoints,
            distance = locations.calculateDistance(),
            travelTime = locations.calculateTravelTime(),
            altitudeRange = minAltitude.toInt() to maxAltitude.toInt(),
            incline = locations.calculateIncline(),
            decline = locations.calculateDecline(),
        )
    }

    fun mapGpxWaypoints(waypoints: MutableList<WayPoint>): List<GpxWaypoint> {
        return waypoints.map { wayPoint ->
            val description = listOfNotNull(
                wayPoint.desc
                    ?.split(":")
                    ?.joinToString("\n"),
                wayPoint.cmt,
            ).joinToString("\n")

            GpxWaypoint(
                name = wayPoint.name,
                description = description,
                location = Location(wayPoint.latitude, wayPoint.longitude, wayPoint.elevation),
            )
        }
    }

}

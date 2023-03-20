package hu.mostoha.mobile.android.huki.model.domain

import io.ticofab.androidgpxparser.parser.domain.WayPoint

data class GpxWaypoint(
    val location: Location,
    val name: String? = null,
)

fun Location.toGpxWaypoint(): GpxWaypoint {
    return GpxWaypoint(
        name = null,
        location = this,
    )
}

fun List<Location>.toGpxWaypointsByLocations(): List<GpxWaypoint> {
    return map { it.toGpxWaypoint() }
}

fun WayPoint.toGpxWaypoint(): GpxWaypoint {
    return GpxWaypoint(
        name = name,
        location = Location(latitude, longitude, elevation),
    )
}

fun List<WayPoint>.toGpxWaypoints(): List<GpxWaypoint> {
    return map { it.toGpxWaypoint() }
}

package hu.mostoha.mobile.android.huki.model.domain

data class GpxWaypoint(
    val location: Location,
    val name: String? = null,
    val description: String? = null,
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

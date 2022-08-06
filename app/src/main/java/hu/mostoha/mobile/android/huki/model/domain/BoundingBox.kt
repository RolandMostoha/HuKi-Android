package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.util.BoundingBox as OsmBoundingBox

data class BoundingBox(
    val north: Double,
    val east: Double,
    val south: Double,
    val west: Double
) {
    override fun toString(): String {
        return "north: %.5f, east: %.5f, south: %.5f, west: %.5f".format(north, east, south, west)
    }
}

fun BoundingBox.toOsmBoundingBox() = OsmBoundingBox(north, east, south, west)

fun OsmBoundingBox.toDomainBoundingBox() = BoundingBox(latNorth, lonEast, latSouth, lonWest)

package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.util.BoundingBox as OsmBoundingBox

data class BoundingBox(
    val north: Double,
    val east: Double,
    val south: Double,
    val west: Double
)

fun BoundingBox.toOsmBoundingBox() = OsmBoundingBox(north, east, south, west)

fun OsmBoundingBox.toDomainBoundingBox() = BoundingBox(latNorth, lonEast, latSouth, lonWest)

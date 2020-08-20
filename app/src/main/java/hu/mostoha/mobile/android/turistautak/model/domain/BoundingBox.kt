package hu.mostoha.mobile.android.turistautak.model.domain

data class BoundingBox(val north: Double, val east: Double, val south: Double, val west: Double)

fun BoundingBox.toOsmBoundingBox() = org.osmdroid.util.BoundingBox(north, east, south, west)

fun org.osmdroid.util.BoundingBox.toDomainBoundingBox() = BoundingBox(latNorth, lonEast, latSouth, lonWest)

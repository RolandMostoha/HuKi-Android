package hu.mostoha.mobile.android.turistautak.domain.model

data class BoundingBox(val north: Double, val east: Double, val south: Double, val west: Double)

fun BoundingBox.toMapBoundingBox() = org.osmdroid.util.BoundingBox(north, east, south, west)

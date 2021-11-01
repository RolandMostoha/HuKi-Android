package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.Location
import kotlin.math.*
import android.location.Location as AndroidLocation


private const val EARTH_RADIUS = 6_372_800

/**
 *  Haversine formula: https://www.movable-type.co.uk/scripts/latlong.html
 */
fun Location.distanceBetween(other: Location): Int {
    val dLat = Math.toRadians(other.latitude - this.latitude)
    val dLon = Math.toRadians(other.longitude - this.longitude)
    val originLat = Math.toRadians(this.latitude)
    val destinationLat = Math.toRadians(other.latitude)

    val a = sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(destinationLat)
    val c = 2 * asin(sqrt(a))
    val distance = EARTH_RADIUS * c
    return distance.roundToInt()
}

fun List<Location>.calculateDistance(): Int {
    var distance = 0
    this.forEachIndexed { index, location ->
        distance += location.distanceBetween(this[min(size - 1, index + 1)])
    }
    return distance
}

private const val ZOOM_LEVEL_MAP_SCALE = 591_657_550
private const val ZOOM_LEVEL_MAP_OFFSET = 4
private const val ZOOM_LEVEL_MAX = 20

/**
 * Algorithm for reasonable zoom level by accuracy: https://gis.stackexchange.com/a/7443
 */
fun AndroidLocation.calculateZoomLevel(): Int {
    return min(
        ZOOM_LEVEL_MAX,
        max(1, log2(ZOOM_LEVEL_MAP_SCALE / accuracy).toInt() - ZOOM_LEVEL_MAP_OFFSET)
    )
}

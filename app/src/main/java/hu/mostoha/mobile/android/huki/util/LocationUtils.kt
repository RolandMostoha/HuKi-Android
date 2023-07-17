package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.Location
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
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
    forEachIndexed { index, location ->
        distance += location.distanceBetween(this[min(size - 1, index + 1)])
    }
    return distance
}

/**
 * Calculates the center of the given [Location]s. It does not accurate for flat 180/-180 degrees.
 */
fun List<Location>.calculateCenter(): Location {
    return Location(
        latitude = this.sumOf { it.latitude } / this.size,
        longitude = this.sumOf { it.longitude } / this.size
    )
}

private const val ZOOM_LEVEL_MAP_SCALE = 591_657_550
private const val ZOOM_LEVEL_MAP_OFFSET = 4
private const val ZOOM_LEVEL_MAX = 16

/**
 * Algorithm for reasonable zoom level by accuracy: https://gis.stackexchange.com/a/7443
 */
fun AndroidLocation.calculateZoomLevel(): Int {
    return min(
        ZOOM_LEVEL_MAX,
        max(1, log2(ZOOM_LEVEL_MAP_SCALE / accuracy).toInt() - ZOOM_LEVEL_MAP_OFFSET)
    )
}

/**
 * Calculates the total incline of the given [Location]s.
 */
fun List<Location>.calculateIncline(): Int {
    val altitudes = mapNotNull { it.altitude?.toInt() }

    var totalIncline = 0

    altitudes.forEachIndexed { index, altitude ->
        val previousAltitude = altitudes.getOrNull(index - 1) ?: return@forEachIndexed
        val incline = if (previousAltitude < altitude) {
            altitude - previousAltitude
        } else {
            0
        }
        totalIncline += incline
    }

    return totalIncline
}

/**
 * Calculates the total decline of the given [Location]s.
 */
fun List<Location>.calculateDecline(): Int {
    val altitudes = mapNotNull { it.altitude?.toInt() }

    var totalDecline = 0

    altitudes.forEachIndexed { index, altitude ->
        val previousAltitude = altitudes.getOrNull(index - 1) ?: return@forEachIndexed
        val incline = if (previousAltitude > altitude) {
            abs(altitude - previousAltitude)
        } else {
            0
        }
        totalDecline += incline
    }

    return totalDecline
}

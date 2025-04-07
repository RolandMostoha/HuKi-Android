package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.domain.toLocations
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
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

private const val WAY_CLOSED_DISTANCE_THRESHOLD_METER = 20

/**
 *  Gets the distance between two [Location]s in meters.
 *
 *  Haversine formula: https://www.movable-type.co.uk/scripts/latlong.html
 */
fun Location.distanceBetween(other: Location): Int {
    val dLat = Math.toRadians(other.latitude - this.latitude)
    val dLon = Math.toRadians(other.longitude - this.longitude)
    val originLat = Math.toRadians(this.latitude)
    val destinationLat = Math.toRadians(other.latitude)

    val a = sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(destinationLat)
    val c = 2 * asin(sqrt(a))
    val distance = EARTH_RADIUS_M * c
    return distance.roundToInt()
}

fun IGeoPoint.distanceBetween(other: IGeoPoint): Int {
    return this.toLocation().distanceBetween(other.toLocation())
}

fun Location.isCloseWithThreshold(other: Location): Boolean {
    return this.distanceBetween(other) <= WAY_CLOSED_DISTANCE_THRESHOLD_METER
}

/**
 * Calculates the total distance between [Location]s in meters.
 */
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

/**
 * Calculates direction arrows from the given [GeoPoint]s with an evenly distributed distance.
 */
@Suppress("MagicNumber")
fun calculateDirectionArrows(geoPoints: List<GeoPoint>): List<Pair<GeoPoint, Int>> {
    if (geoPoints.isEmpty()) {
        return emptyList()
    }

    val totalDistance = geoPoints.toLocations().calculateDistance()
    val arrowDistance = when {
        totalDistance < 2_000 -> 50
        totalDistance < 5_000 -> 100
        totalDistance < 10_000 -> 300
        totalDistance < 20_000 -> 500
        totalDistance < 50_000 -> 1_000
        else -> 2_000
    }

    val arrows = geoPoints.mapIndexedNotNull { index, geoPoint ->
        if (index == geoPoints.lastIndex) {
            return@mapIndexedNotNull null
        }

        val nextGeoPoint = geoPoints[index + 1]
        val bearing = geoPoint.bearingTo(nextGeoPoint).toInt()
        val distance = geoPoint.distanceBetween(nextGeoPoint)

        Triple(geoPoint, bearing, distance)
    }

    val distributedArrows = mutableListOf<Pair<GeoPoint, Int>>()
    var accumulatedDistance = 0

    val firstArrow = arrows.first()
    distributedArrows.add(firstArrow.first to firstArrow.second)

    for (arrow in arrows) {
        accumulatedDistance += arrow.third
        if (accumulatedDistance >= arrowDistance) {
            distributedArrows.add(arrow.first to arrow.second)
            accumulatedDistance = 0
        }
    }

    return distributedArrows
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

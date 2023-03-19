package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.Location
import kotlin.math.abs
import kotlin.math.atan
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Suppress("MagicNumber")
fun List<Location>.calculateTravelTime(): Duration {
    val locations = this.map { Location(it.latitude, it.longitude, it.altitude ?: 0.0) }

    var totalTimeHours = 0.0

    locations.forEachIndexed { index, location ->
        val previousLocation = locations.getOrNull(index - 1) ?: return@forEachIndexed

        val segmentDistanceM = location.distanceBetween(previousLocation).toDouble()
        val inclineM = (location.altitude!! - previousLocation.altitude!!)

        val segmentTimeHours = naismith(segmentDistanceM / 1000, inclineM / 1000)

        totalTimeHours += segmentTimeHours
    }

    return totalTimeHours.hours
}

/**
 * Calculates the hiking travel time by Naismith's rule with Aitken-Langmuir corrections.
 * It takes into account: distance, incline, decline, speed
 *
 * Source: https://medium.com/@sunside/naismith-aitken-langmuir-tranter-and-tobler-modeling-hiking-speed-4ff3937e6898
 */
@Suppress("MagicNumber")
fun naismith(distanceKm: Double, inclineKm: Double, baseSpeedKmH: Double = 4.0): Double {
    if (distanceKm == 0.0) return 0.0

    val slope = inclineKm / distanceKm

    var timeHours = distanceKm * (1.0 / baseSpeedKmH)

    if (slope >= 0.0) {
        timeHours += inclineKm * (1.0 / 0.6)
    } else if (atan(slope) <= -5.0 && atan(slope) >= -12.0) {
        timeHours -= abs(inclineKm) * ((10.0 / 60.0) / 0.3)
    } else if (atan(slope) < -12.0) {
        timeHours += abs(inclineKm) * ((10.0 / 60.0) / 0.3)
    }

    return timeHours

}

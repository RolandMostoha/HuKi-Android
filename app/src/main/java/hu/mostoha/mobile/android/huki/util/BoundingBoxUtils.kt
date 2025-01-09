package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import kotlin.math.cos

@Suppress("MagicNumber")
fun BoundingBox.extendByDistance(distanceKm: Int): BoundingBox {
    val latExtension = distanceKm / EARTH_RADIUS_KM * (180 / Math.PI)
    val lonExtension = distanceKm / (EARTH_RADIUS_KM * cos(Math.toRadians((north + south) / 2))) * (180 / Math.PI)

    return BoundingBox(
        north = north + latExtension,
        south = south - latExtension,
        east = east + lonExtension,
        west = west - lonExtension
    )
}

fun BoundingBox.areaDistance(): Int {
    return Location(east, north).distanceBetween(Location(west, south)) / 2
}

fun BoundingBox.areaDistanceMessage(): Message {
    val areaDistance = this.areaDistance()

    return Message.Res(
        res = R.string.place_area_distance_template,
        formatArgs = listOf(DistanceFormatter.formatWithoutScale(areaDistance))
    )
}

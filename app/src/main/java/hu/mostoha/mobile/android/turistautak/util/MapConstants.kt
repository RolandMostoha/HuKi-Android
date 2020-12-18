package hu.mostoha.mobile.android.turistautak.util

import hu.mostoha.mobile.android.turistautak.model.domain.BoundingBox

const val MY_LOCATION_DEFAULT_ZOOM = 13.0
const val MY_LOCATION_MIN_TIME_MS = 3000L
const val MY_LOCATION_MIN_DISTANCE_METER = 3f

val HUNGARY = BoundingBox(48.62385, 22.71053, 45.75948, 16.20229)

const val MAP_DEFAULT_ZOOM_LEVEL = 15.0
const val MAP_TILES_SCALE_FACTOR = 1.5f
const val MAP_ZOOM_THRESHOLD_ROUTES_NEARBY = 8.5

object OverlayPositions {
    const val HIKING_LAYER = 0
    const val MY_LOCATION = 1
    const val PLACE = 2
}

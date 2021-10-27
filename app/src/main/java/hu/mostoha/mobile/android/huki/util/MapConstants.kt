package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox

const val MY_LOCATION_DEFAULT_ZOOM = 13.0
const val MY_LOCATION_MIN_TIME_MS = 3000L
const val MY_LOCATION_MIN_DISTANCE_METER = 3f

const val HUNGARY_BOX_NORTH = 48.62385
const val HUNGARY_BOX_WEST = 16.20229
const val HUNGARY_BOX_EAST = 22.71053
const val HUNGARY_BOX_SOUTH = 45.75948

val HUNGARY = BoundingBox(
    north = HUNGARY_BOX_NORTH,
    east = HUNGARY_BOX_WEST,
    south = HUNGARY_BOX_EAST,
    west = HUNGARY_BOX_SOUTH,
)

const val MAP_DEFAULT_ZOOM_LEVEL = 15.0
const val MAP_TILES_SCALE_FACTOR = 1.6f
const val MAP_ZOOM_THRESHOLD_ROUTES_NEARBY = 8.5

object OverlayPositions {
    const val HIKING_LAYER = 0
    const val MY_LOCATION = 1
    const val PLACE = 2
}

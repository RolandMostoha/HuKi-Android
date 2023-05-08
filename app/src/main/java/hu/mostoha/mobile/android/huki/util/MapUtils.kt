package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location

const val MAP_DEFAULT_ZOOM_LEVEL = 15.0
const val MAP_DEFAULT_SCALE_FACTOR = 1.5

const val MY_LOCATION_TIME_MS = 10000L
const val MY_LOCATION_MIN_INTERVAL_TIME_MS = 4000L

const val HUNGARY_BOX_NORTH = 48.62385
const val HUNGARY_BOX_WEST = 16.20229
const val HUNGARY_BOX_EAST = 22.71053
const val HUNGARY_BOX_SOUTH = 45.75948
val HUNGARY_BOUNDING_BOX = BoundingBox(
    north = HUNGARY_BOX_NORTH,
    east = HUNGARY_BOX_EAST,
    south = HUNGARY_BOX_SOUTH,
    west = HUNGARY_BOX_WEST
)

private const val BUDAPEST_LATITUDE = 47.4933876
private const val BUDAPEST_LONGITUDE = 19.0548635
val BUDAPEST_LOCATION = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

const val WAY_CLOSED_DISTANCE_THRESHOLD_METER = 20

const val OSM_ID_UNAVAILABLE = Integer.MIN_VALUE.toString()

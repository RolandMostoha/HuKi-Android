package hu.mostoha.mobile.android.huki.util

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import org.osmdroid.util.GeoPoint

const val MAP_DEFAULT_ZOOM_LEVEL = 15.0
const val MAP_TILES_SCALE_FACTOR = 1.4f
const val MAP_ZOOM_THRESHOLD_ROUTES_NEARBY = 8.5

const val MY_LOCATION_TIME_MS = 10000L
const val MY_LOCATION_MIN_TIME_MS = 4000L

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

fun AppCompatActivity.startGoogleDirections(geoPoint: GeoPoint) {
    val latLng = "${geoPoint.latitude},${geoPoint.longitude}"
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latLng")

    startActivity(Intent(Intent.ACTION_VIEW, uri))
}

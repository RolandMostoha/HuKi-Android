package hu.mostoha.mobile.android.turistautak.extensions

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Int) {
    controller.setZoom(zoomLevel.toDouble())
    controller.setCenter(geoPoint)
}
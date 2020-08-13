package hu.mostoha.mobile.android.turistautak.extensions

import android.graphics.Color
import android.graphics.drawable.Drawable
import kotlinx.android.synthetic.main.activity_home.view.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

fun MapView.addMarker(geoPoint: GeoPoint, icon: Drawable) {
    val marker = Marker(this).apply {
        position = geoPoint
        this.icon = icon
    }
    homeMapView.overlays.add(marker)
    homeMapView.invalidate()
}

fun MapView.addPolygon(geoPoints: List<GeoPoint>) {
    val polygon = Polygon().apply {
        fillPaint.color = Color.parseColor("#1EFFE70E")
        points = geoPoints
    }
    homeMapView.overlays.add(polygon)
    homeMapView.invalidate()
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Int) {
    controller.setZoom(zoomLevel.toDouble())
    controller.setCenter(geoPoint)
}
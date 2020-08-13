package hu.mostoha.mobile.android.turistautak.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
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

fun MapView.addPolygon(geoPoints: List<GeoPoint>, @ColorInt strokeColor: Int, @ColorInt fillColor: Int) {
    val polygon = Polygon().apply {
        outlinePaint.color = strokeColor
        outlinePaint.strokeWidth = 5f
        fillPaint.color = fillColor
        points = geoPoints
    }
    homeMapView.overlays.add(polygon)
    homeMapView.invalidate()
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Int) {
    controller.setZoom(zoomLevel.toDouble())
    controller.setCenter(geoPoint)
}
package hu.mostoha.mobile.android.turistautak.extensions

import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
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

fun MapView.addPolygon(
    geoPoints: List<GeoPoint>,
    @DimenRes strokeWidth: Int,
    @ColorRes strokeColor: Int,
    @ColorRes fillColor: Int
) {
    val context = this@addPolygon.context
    val polygon = Polygon().apply {
        outlinePaint.apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, strokeColor)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = context.resources.getDimension(strokeWidth)
        }
        fillPaint.color = ContextCompat.getColor(context, fillColor)
        points = geoPoints
    }
    overlays.add(polygon)
    invalidate()
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Int) {
    controller.setZoom(zoomLevel.toDouble())
    controller.setCenter(geoPoint)
}
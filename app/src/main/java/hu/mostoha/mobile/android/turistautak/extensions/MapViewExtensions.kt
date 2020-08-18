package hu.mostoha.mobile.android.turistautak.extensions

import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.turistautak.R
import kotlinx.android.synthetic.main.activity_home.view.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

fun MapView.addMarker(geoPoint: GeoPoint, icon: Drawable, onClick: (Marker) -> Unit): Marker {
    val marker = Marker(this).apply {
        position = geoPoint
        this.icon = icon
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker)
            true
        }
    }
    homeMapView.overlays.add(marker)
    homeMapView.invalidate()

    return marker
}

fun MapView.removeMarker(marker: Marker) {
    marker.remove(homeMapView)
    homeMapView.invalidate()
}

fun MapView.addPolygon(
    geoPoints: List<GeoPoint>,
    onClick: (Polygon) -> Unit,
    @DimenRes strokeWidth: Int = R.dimen.default_polyline_stroke_width,
    @ColorRes strokeColor: Int = R.color.colorPolyline,
    @ColorRes fillColor: Int = R.color.colorPolylineFill
): Polygon {
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
        setOnClickListener { polygon, _, _ ->
            onClick.invoke(polygon)
            true
        }
    }
    overlays.add(polygon)
    invalidate()

    return polygon
}

fun MapView.removePolygon(polygon: Polygon) {
    homeMapView.overlays.remove(polygon)
    homeMapView.invalidate()
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Int) {
    controller.setZoom(zoomLevel.toDouble())
    controller.setCenter(geoPoint)
}

fun MapView.animateCenterAndZoom(geoPoint: GeoPoint, zoomLevel: Int) {
    controller.animateTo(geoPoint, zoomLevel.toDouble(), 1000)
}
package hu.mostoha.mobile.android.huki.extensions

import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

fun MapView.addOverlay(position: Int, overlay: Overlay) {
    if (position > overlays.size - 1) {
        overlays.add(overlay)
    } else {
        overlays.add(position, overlay)
    }
    invalidate()
}

fun MapView.removeOverlay(vararg overlay: Overlay) {
    overlay.forEach {
        overlays.remove(it)
    }
    invalidate()
}

fun MapView.removeOverlays() {
    overlays
        .takeLast(overlays.size - 1)
        .forEach {
            overlays.remove(it)
        }
    invalidate()
}

fun MapView.addMarker(geoPoint: GeoPoint, icon: Drawable, onClick: (Marker) -> Unit): Marker {
    val marker = Marker(this).apply {
        position = geoPoint
        this.icon = icon
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker)
            true
        }
    }
    overlays.add(marker)
    invalidate()

    return marker
}

fun MapView.removeMarker(marker: Marker) {
    marker.remove(this)
    invalidate()
}

fun MapView.addPolygon(
    geoPoints: List<GeoPoint>,
    onClick: (PolyOverlayWithIW) -> Unit,
    @DimenRes strokeWidth: Int = R.dimen.default_polygon_stroke_width,
    @ColorRes strokeColor: Int = R.color.colorPolyline,
    @ColorRes fillColor: Int = R.color.colorPolylineFill
): PolyOverlayWithIW {
    val context = this.context
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

fun MapView.addPolyline(
    geoPoints: List<GeoPoint>,
    onClick: (PolyOverlayWithIW) -> Unit,
    @DimenRes strokeWidth: Int = R.dimen.default_polyline_stroke_width,
    @ColorRes strokeColor: Int = R.color.colorPolyline
): PolyOverlayWithIW {
    val context = this.context
    val polyline = Polyline().apply {
        outlinePaint.apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, strokeColor)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = context.resources.getDimension(strokeWidth)
        }
        setPoints(geoPoints)
        setOnClickListener { polygon, _, _ ->
            onClick.invoke(polygon)
            true
        }
    }
    overlays.add(polyline)
    invalidate()

    return polyline
}

fun MapView.addFastOverlay(
    geoPoints: List<GeoPoint>,
    onClick: (SimpleFastPointOverlay) -> Unit,
    @ColorRes pointColor: Int = R.color.colorPolyline
): SimpleFastPointOverlay {
    val context = this.context
    val points = geoPoints.map { LabelledGeoPoint(it) }
    val pointStyle = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, pointColor)
    }
    val options = SimpleFastPointOverlayOptions.getDefaultStyle()
        .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.NO_OPTIMIZATION)
        .setSymbol(SimpleFastPointOverlayOptions.Shape.CIRCLE)
        .setPointStyle(pointStyle)
        .setRadius(7f)
        .setIsClickable(true)

    val fastOverlay = SimpleFastPointOverlay(SimplePointTheme(points, true), options)
    fastOverlay.setOnClickListener { _, _ ->
        onClick.invoke(fastOverlay)
    }

    overlays.add(fastOverlay)
    invalidate()

    return fastOverlay
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Double) {
    controller.setZoom(zoomLevel)
    controller.setCenter(geoPoint)
}

fun MapView.animateCenterAndZoom(geoPoint: GeoPoint, zoomLevel: Double) {
    controller.animateTo(geoPoint, zoomLevel, 1000)
}

fun MapView.addZoomListener(onZoom: (event: ZoomEvent) -> Unit) {
    addMapListener(object : MapListener {
        override fun onScroll(event: ScrollEvent): Boolean {
            return false
        }

        override fun onZoom(event: ZoomEvent): Boolean {
            onZoom.invoke(event)
            return true
        }
    })
}
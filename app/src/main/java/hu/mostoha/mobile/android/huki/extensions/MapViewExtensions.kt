package hu.mostoha.mobile.android.huki.extensions

import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.MapInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OVERLAY_TYPE_ORDER_MAP
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerPolyline
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.getGradientColors
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.PolyOverlayWithIW
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingCycle
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList
import java.util.UUID

private const val MAP_ANIMATION_DURATION = 1000L

fun MapView.addOverlay(overlay: Overlay, comparator: Comparator<Overlay>) {
    overlays.add(overlay)
    overlays.sortWith(comparator)
    invalidate()
}

fun MapView.hasOverlay(overlayId: String): Boolean {
    return overlays.any { it is OverlayWithIW && it.id == overlayId }
}

fun MapView.hideOverlay(overlayId: String) {
    overlays.filterIsInstance<OverlayWithIW>()
        .filter { it.id == overlayId }
        .forEach { it.isEnabled = false }
    invalidate()
}

fun MapView.showOverlay(overlayId: String) {
    overlays.filterIsInstance<OverlayWithIW>()
        .filter { it.id == overlayId }
        .forEach { it.isEnabled = true }
    invalidate()
}

fun MapView.removeOverlay(overlay: List<Overlay>) {
    overlay.forEach {
        overlays.remove(it)
    }
    invalidate()
}

fun MapView.removeOverlay(overlayId: String) {
    overlays.removeAll { it is OverlayWithIW && it.id == overlayId }
    invalidate()
}

fun MapView.removeOverlay(overlayType: OverlayType) {
    val overlayClasses = OVERLAY_TYPE_ORDER_MAP.getValue(overlayType)
    overlays.removeIf { overlayClasses.contains(it::class) }
    invalidate()
}

fun MapView.addMarker(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    onClick: (Marker) -> Unit
): Marker {
    val marker = Marker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
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

fun MapView.addPolyline(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoints: List<GeoPoint>,
    onClick: (PolyOverlayWithIW) -> Unit,
    @DimenRes strokeWidthRes: Int = R.dimen.default_polyline_width,
    @ColorRes strokeColorRes: Int = R.color.colorPolyline
): Polyline {
    val context = this.context
    val polyline = Polyline(this).apply {
        id = overlayId
        outlinePaint.apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, strokeColorRes)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isGeodesic = true
            strokeWidth = context.resources.getDimension(strokeWidthRes)
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

fun MapView.addPolygon(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoints: List<GeoPoint>,
    onClick: (PolyOverlayWithIW) -> Unit,
    @ColorRes strokeColorRes: Int = R.color.colorPolyline,
    @ColorRes fillColorRes: Int = R.color.colorPolylineFill
): Polygon {
    val context = this.context
    val polygon = Polygon().apply {
        id = overlayId
        outlinePaint.apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, strokeColorRes)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.resources.getDimension(R.dimen.default_polygon_width)
        }
        fillPaint.color = ContextCompat.getColor(context, fillColorRes)
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

fun MapView.addGpxMarker(
    overlayId: String,
    geoPoint: GeoPoint,
    waypointType: WaypointType,
    infoWindowTitle: String? = null,
    onClick: (Marker) -> Unit
): Marker {
    val iconDrawable = when (waypointType) {
        WaypointType.START -> R.drawable.ic_marker_gpx_start.toDrawable(this.context)
        WaypointType.INTERMEDIATE -> R.drawable.ic_marker_gpx_intermediate.toDrawable(this.context)
        WaypointType.END -> R.drawable.ic_marker_gpx_end.toDrawable(this.context)
    }
    val marker = GpxMarker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable

        if (infoWindowTitle != null) {
            infoWindow = MapInfoWindow(this@addGpxMarker, infoWindowTitle)
            title = infoWindowTitle
        } else {
            setOnMarkerClickListener { marker, _ ->
                onClick.invoke(marker)
                true
            }
        }
    }
    overlays.add(marker)
    invalidate()

    return marker
}

fun MapView.addGpxPolyline(
    overlayId: String,
    geoPoints: List<GeoPoint>,
    useAltitudeColors: Boolean,
    onClick: (PolyOverlayWithIW) -> Unit
): Polyline {
    val context = this.context
    val polyline = GpxPolyline(this).apply {
        id = overlayId

        setPoints(geoPoints)

        val borderPaint = Paint().apply {
            color = if (useAltitudeColors) {
                ContextCompat.getColor(context, R.color.colorPolylineBorder)
            } else {
                ContextCompat.getColor(context, R.color.colorPolyline)
            }
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_width)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        outlinePaintLists.add(MonochromaticPaintList(borderPaint))

        if (useAltitudeColors) {
            val fillPaint = Paint().apply {
                isAntiAlias = true
                strokeWidth = context.resources.getDimension(R.dimen.default_polyline_fill_width)
                style = Paint.Style.FILL_AND_STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }
            val gradientColors = getGradientColors(
                ContextCompat.getColor(context, R.color.colorPolyline),
                ContextCompat.getColor(context, R.color.colorPolylineHigh),
                geoPoints.map { it.altitude.toFloat() }
            )
            val colorMapping = ColorMappingCycle(gradientColors)
            outlinePaintLists.add(PolychromaticPaintList(fillPaint, colorMapping, false))
        }

        setOnClickListener { polygon, _, _ ->
            onClick.invoke(polygon)
            true
        }
    }

    overlays.add(polyline)
    invalidate()

    return polyline
}

fun MapView.addRoutePlannerMarker(
    overlayId: String,
    geoPoint: GeoPoint,
    waypointType: WaypointType,
    onClick: (Marker) -> Unit
): Marker {
    val iconDrawable = when (waypointType) {
        WaypointType.START -> R.drawable.ic_marker_gpx_start.toDrawable(this.context)
        WaypointType.INTERMEDIATE -> R.drawable.ic_marker_gpx_intermediate.toDrawable(this.context)
        WaypointType.END -> R.drawable.ic_marker_gpx_end.toDrawable(this.context)
    }
    val marker = RoutePlannerMarker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker)
            true
        }
    }
    overlays.add(marker)
    invalidate()

    return marker
}

fun MapView.addRoutePlannerPolyline(
    overlayId: String,
    geoPoints: List<GeoPoint>,
    onClick: (PolyOverlayWithIW) -> Unit
): Polyline {
    val context = this.context
    val polyline = RoutePlannerPolyline(this).apply {
        id = overlayId

        val borderPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPolylineBorder)
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_width)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        outlinePaintLists.add(MonochromaticPaintList(borderPaint))
        val fillPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPolyline)
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_fill_width)
            style = Paint.Style.FILL_AND_STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        outlinePaintLists.add(MonochromaticPaintList(fillPaint))

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

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Double) {
    controller.setZoom(zoomLevel)
    controller.setCenter(geoPoint)
}

fun MapView.animateCenterAndZoom(geoPoint: GeoPoint, zoomLevel: Double) {
    controller.animateTo(geoPoint, zoomLevel, MAP_ANIMATION_DURATION)
}

fun MapView.addMapMovedListener(onMapMoved: () -> Unit) {
    addMapListener(object : MapListener {
        override fun onScroll(event: ScrollEvent): Boolean {
            onMapMoved.invoke()
            return true
        }

        override fun onZoom(event: ZoomEvent): Boolean {
            onMapMoved.invoke()
            return true
        }
    })
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

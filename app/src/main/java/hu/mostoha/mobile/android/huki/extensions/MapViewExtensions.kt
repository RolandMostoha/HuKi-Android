package hu.mostoha.mobile.android.huki.extensions

import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.GpxMarkerInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.LocationPickerInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolygon
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LocationPickerMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OVERLAY_TYPE_ORDER_MAP
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktBasePolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceDetailsMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerPolyline
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.getGradientColors
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.PolyOverlayWithIW
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingCycle
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.util.UUID

private const val MAP_ANIMATION_DURATION = 1000L

fun MapView.addOverlay(overlay: Overlay, comparator: Comparator<Overlay>) {
    overlays.add(overlay)
    overlays.sortWith(comparator)
    invalidate()
}

fun MapView.addOverlays(overlayList: List<Overlay>, comparator: Comparator<Overlay>) {
    overlays.addAll(overlayList)
    overlays.sortWith(comparator)
    invalidate()
}

fun MapView.hasOverlay(overlayId: String): Boolean {
    return overlays.any { it is OverlayWithIW && it.id == overlayId }
}

fun MapView.hasNoOverlay(overlayId: String): Boolean {
    return !hasOverlay(overlayId)
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

inline fun <reified T : OverlayWithIW> MapView.switchOverlayVisibility(overlayId: String) {
    overlays.filterIsInstance<T>()
        .filter { it.id == overlayId }
        .forEach { it.isEnabled = !it.isEnabled }
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

fun MapView.removeOverlays(overlayTypes: List<OverlayType>) {
    overlayTypes.forEach { overlayType ->
        val overlayClasses = OVERLAY_TYPE_ORDER_MAP.getValue(overlayType)
        overlays.removeIf { overlayClasses.contains(it::class) }
        invalidate()
    }
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

    addOverlay(marker, OverlayComparator)

    return marker
}

fun MapView.addPlaceDetailsMarker(
    overlayId: String = UUID.randomUUID().toString(),
    name: Message,
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    onClick: (PlaceDetailsMarker) -> Unit
): Marker {
    val marker = PlaceDetailsMarker(this, name).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker as PlaceDetailsMarker)
            true
        }
    }

    addOverlay(marker, OverlayComparator)

    return marker
}

fun MapView.removeMarker(marker: Marker) {
    marker.remove(this)
    invalidate()
}

fun MapView.addPolyline(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoints: List<GeoPoint>,
    @ColorInt colorInt: Int = ContextCompat.getColor(context, R.color.colorPolyline),
    onClick: () -> Unit,
): Polyline {
    val context = this.context
    val polyline = Polyline(this).apply {
        id = overlayId
        val borderPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPolylineBorder)
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_width)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        outlinePaintLists.add(MonochromaticPaintList(borderPaint))
        val fillPaint = Paint().apply {
            color = colorInt
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_fill_width)
            style = Paint.Style.FILL_AND_STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        outlinePaintLists.add(MonochromaticPaintList(fillPaint))
        setPoints(geoPoints)
        setOnClickListener { _, _, _ ->
            onClick.invoke()
            true
        }
    }

    addOverlay(polyline, OverlayComparator)

    return polyline
}

fun MapView.addPolygon(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoints: List<GeoPoint>,
    onClick: () -> Unit,
): Polygon {
    val context = this.context
    val polygon = Polygon().apply {
        id = overlayId
        outlinePaint.apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.colorMarker)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = context.resources.getDimension(R.dimen.default_polygon_width)
        }
        fillPaint.color = ContextCompat.getColor(context, R.color.colorPolylineFill)
        points = geoPoints
        setOnClickListener { _, _, _ ->
            onClick.invoke()
            true
        }
    }

    addOverlay(polygon, OverlayComparator)

    return polygon
}

fun MapView.addLandscapePolyOverlay(
    overlayId: String,
    center: GeoPoint,
    centerMarkerDrawable: Drawable,
    way: GeometryUiModel.Way,
    onClick: (OverlayWithIW) -> Unit,
): List<OverlayWithIW> {
    val context = this.context
    val geoPoints = way.geoPoints

    val resultOverlays = mutableListOf<OverlayWithIW>()

    val polyOverlay = if (way.isClosed) {
        LandscapePolygon(this).apply {
            id = overlayId
            outlinePaint.apply {
                isAntiAlias = true
                color = ContextCompat.getColor(context, R.color.colorMarker)
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = context.resources.getDimension(R.dimen.default_polygon_width)
            }
            fillPaint.color = ContextCompat.getColor(context, R.color.colorPolylineFill)
            points = geoPoints
            setOnClickListener { polygon, _, _ ->
                onClick.invoke(polygon)
                true
            }
        }
    } else {
        LandscapePolyline(this).apply {
            id = overlayId
            val borderPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.colorPolylineBorder)
                isAntiAlias = true
                strokeWidth = context.resources.getDimension(R.dimen.default_polyline_width)
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            outlinePaintLists.add(MonochromaticPaintList(borderPaint))
            val fillPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.colorPolyline)
                isAntiAlias = true
                strokeWidth = context.resources.getDimension(R.dimen.default_polyline_fill_width)
                style = Paint.Style.FILL_AND_STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            outlinePaintLists.add(MonochromaticPaintList(fillPaint))
            setPoints(geoPoints)
            setOnClickListener { polygon, _, _ ->
                onClick.invoke(polygon)
                true
            }
        }
    }

    val marker = LandscapeMarker(this).apply {
        id = overlayId
        position = center
        icon = centerMarkerDrawable
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker)
            true
        }
    }

    addOverlays(listOf(polyOverlay, marker), OverlayComparator)

    return resultOverlays
}

fun MapView.addGpxMarker(
    overlayId: String,
    geoPoint: GeoPoint,
    waypointType: WaypointType,
    infoWindowTitle: String? = null,
    infoWindowDescription: String? = null,
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
            infoWindow = GpxMarkerInfoWindow(
                mapView = this@addGpxMarker,
                title = infoWindowTitle,
                description = infoWindowDescription,
            )
            title = infoWindowTitle
        } else {
            setOnMarkerClickListener { marker, _ ->
                onClick.invoke(marker)
                true
            }
        }
    }

    addOverlay(marker, OverlayComparator)

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

    addOverlay(polyline, OverlayComparator)

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

    addOverlay(marker, OverlayComparator)

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

    addOverlay(polyline, OverlayComparator)

    return polyline
}

fun MapView.addLocationPickerMarker(
    geoPoint: GeoPoint,
    onSaveClick: (GeoPoint) -> Unit,
    onCloseClick: (() -> Unit)? = null,
): Marker {
    removeOverlay(OverlayType.LOCATION_PICKER)
    InfoWindow.closeAllInfoWindowsOn(this)

    val mapView = this@addLocationPickerMarker
    var markerGeoPoint = geoPoint

    val marker = LocationPickerMarker(mapView).apply {
        id = UUID.randomUUID().toString()
        position = geoPoint
        icon = R.drawable.ic_marker_location_picker.toDrawable(mapView.context)
        infoWindow = LocationPickerInfoWindow(
            mapView = mapView,
            onSaveClick = {
                closeInfoWindow()
                mapView.removeOverlay(OverlayType.LOCATION_PICKER)
                onSaveClick.invoke(markerGeoPoint)
            },
            onCloseClick = {
                closeInfoWindow()
                mapView.removeOverlay(OverlayType.LOCATION_PICKER)
                onCloseClick?.invoke()
            }
        )
        isDraggable = true
        setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker) {
                // no-op
            }

            override fun onMarkerDragEnd(marker: Marker) {
                markerGeoPoint = marker.position
                showInfoWindow()
            }

            override fun onMarkerDragStart(marker: Marker) {
                // no-op
            }
        })
    }

    addOverlay(marker, OverlayComparator)

    marker.showInfoWindow()

    return marker
}

fun MapView.addHikingRouteDetails(
    overlayId: String,
    relation: GeometryUiModel.HikingRoute,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
): List<PolyOverlayWithIW> {
    val overlays = mutableListOf<PolyOverlayWithIW>()

    relation.ways.forEach { way ->
        val overlay = addPolyline(
            overlayId = overlayId,
            geoPoints = way.geoPoints,
            onClick = { onClick.invoke() }
        )
        overlays.add(overlay)
    }

    relation.waypoints.forEach { geoPoint ->
        addMarker(
            overlayId = overlayId,
            geoPoint = geoPoint,
            iconDrawable = generateLayerDrawable(
                layers = listOf(
                    LayerDrawableConfig(
                        R.drawable.ic_marker_hiking_route_background.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.hiking_routes_marker_background_size)
                    ),
                    LayerDrawableConfig(
                        iconRes.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.hiking_routes_marker_icon_size)
                    ),
                ),
            ),
            onClick = { onClick.invoke() },
        )
    }

    return overlays
}

fun MapView.addOktBasePolyline(
    overlayId: String,
    geoPoints: List<GeoPoint>,
    onClick: (GeoPoint) -> Unit,
) {
    val context = this.context
    val polyline = OktBasePolyline(this).apply {
        id = overlayId
        outlinePaint.apply {
            color = ContextCompat.getColor(context, R.color.colorOktBlue)
            strokeWidth = context.resources.getDimension(R.dimen.okt_routes_base_polyline_width)
        }
        setPoints(geoPoints)
        setOnClickListener { _, _, geoPoint ->
            onClick.invoke(geoPoint)
            true
        }
    }

    addOverlay(polyline, OverlayComparator)
}

fun MapView.addOktRoute(
    overlayId: String,
    oktRouteUiModel: OktRouteUiModel,
    onClick: () -> Unit,
) {
    if (oktRouteUiModel.id != OKT_ID_FULL_ROUTE) {
        addOktPolyline(
            overlayId = overlayId,
            geoPoints = oktRouteUiModel.geoPoints,
            onClick = { onClick.invoke() }
        )
    }

    listOf(oktRouteUiModel.start, oktRouteUiModel.end).forEach {
        addOktMarker(
            overlayId = overlayId,
            geoPoint = it,
            iconDrawable = generateLayerDrawable(
                layers = listOf(
                    LayerDrawableConfig(
                        R.drawable.ic_marker_okt_route_background.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.hiking_routes_marker_background_size)
                    ),
                    LayerDrawableConfig(
                        R.drawable.ic_marker_okt_routes.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.hiking_routes_marker_icon_size)
                    ),
                ),
            ),
            onClick = { onClick.invoke() },
        )
    }
}

fun MapView.addOktMarker(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    onClick: (Marker) -> Unit
) {
    val marker = OktMarker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker)
            true
        }
    }

    addOverlay(marker, OverlayComparator)
}

fun MapView.addOktPolyline(
    overlayId: String,
    geoPoints: List<GeoPoint>,
    onClick: () -> Unit,
) {
    val context = this.context
    val polyline = OktPolyline(this).apply {
        id = overlayId
        outlinePaint.apply {
            color = ContextCompat.getColor(context, R.color.colorOktBlue)
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.okt_routes_polyline_width)
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        setPoints(geoPoints)
        setOnClickListener { _, _, _ ->
            onClick.invoke()
            true
        }
    }

    addOverlay(polyline, OverlayComparator)
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Double) {
    controller.setZoom(zoomLevel)
    controller.setCenter(geoPoint)
}

fun MapView.animateCenterAndZoom(geoPoint: GeoPoint, zoomLevel: Double) {
    controller.animateTo(geoPoint, zoomLevel, MAP_ANIMATION_DURATION)
}

fun MapView.zoomToBoundingBoxPostMain(boundingBox: BoundingBox, animated: Boolean) {
    postMain {
        this.zoomToBoundingBox(boundingBox, animated)
    }
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

fun MapView.addLongClickHandlerOverlay(onLongClick: (GeoPoint) -> Unit) {
    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            return false
        }

        override fun longPressHelper(p: GeoPoint): Boolean {
            onLongClick.invoke(p)
            return true
        }
    })
    addOverlay(mapEventsOverlay, OverlayComparator)
}

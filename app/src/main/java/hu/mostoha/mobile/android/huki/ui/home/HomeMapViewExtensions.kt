package hu.mostoha.mobile.android.huki.ui.home

import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.Paint.Style
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.R.dimen.landscapes_polyline_width
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.extensions.LayerDrawableConfig
import hu.mostoha.mobile.android.huki.extensions.addMarker
import hu.mostoha.mobile.android.huki.extensions.addOverlay
import hu.mostoha.mobile.android.huki.extensions.closeInfoWindows
import hu.mostoha.mobile.android.huki.extensions.generateLayerDrawable
import hu.mostoha.mobile.android.huki.extensions.hasOverlay
import hu.mostoha.mobile.android.huki.extensions.removeOverlay
import hu.mostoha.mobile.android.huki.extensions.replaceOverlay
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.model.domain.DestinationType
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.resolveIcon
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocationsWithAlt
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.DistanceInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.LocationPickerInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.NavigationMarkerInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxArrowMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.HukiScaleBarOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeDetailsDestinationMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeDetailsPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapeMapDestinationMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolygon
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LandscapePolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.LocationPickerMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktBasePolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OktPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceCategoryMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceDetailsDestinationMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RoutePlannerPolyline
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.SLOPE_PERCENTAGE_HIGH
import hu.mostoha.mobile.android.huki.util.SLOPE_PERCENTAGE_MID
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.getSlopeGradientColors
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.PolyOverlayWithIW
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.ColorMappingCycle
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList
import org.osmdroid.views.overlay.advancedpolyline.PolychromaticPaintList
import java.util.UUID

private const val DIRECTION_ARROW_ICON_ANCHOR = 0.5f

fun MapView.addPolyline(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoints: List<GeoPoint>,
    @ColorInt colorBorder: Int = ContextCompat.getColor(context, R.color.colorPolylineBorder),
    @ColorInt colorFill: Int = ContextCompat.getColor(context, R.color.colorPolyline),
    onClick: () -> Unit,
): Polyline {
    val context = this.context
    val polyline = Polyline(this).apply {
        id = overlayId
        val borderPaint = Paint().apply {
            color = colorBorder
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_width)
            style = Style.STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
        }
        outlinePaintLists.add(MonochromaticPaintList(borderPaint))
        val fillPaint = Paint().apply {
            color = colorFill
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_polyline_fill_width)
            style = Style.FILL_AND_STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
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
        fillPaint.color = ContextCompat.getColor(context, R.color.colorPolygonFill)
        points = geoPoints
        setOnClickListener { _, _, _ ->
            onClick.invoke()
            true
        }
    }

    addOverlay(polygon, OverlayComparator)

    return polygon
}

fun MapView.addLandscapePolygon(
    overlayId: String,
    way: GeometryUiModel.Way,
    @ColorInt fillColor: Int = ContextCompat.getColor(context, R.color.colorPolygonFill),
    onClick: (OverlayWithIW) -> Unit,
): OverlayWithIW {
    val context = this.context
    val geoPoints = way.geoPoints

    val polygon = LandscapePolygon(this).apply {
        id = overlayId
        outlinePaint.apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.colorPolylineBorder)
            style = Style.STROKE
            strokeCap = Cap.ROUND
            strokeWidth = context.resources.getDimension(R.dimen.default_polygon_width)
        }
        fillPaint.color = fillColor
        points = geoPoints
        setOnClickListener { polygon, _, _ ->
            onClick.invoke(polygon)
            true
        }
    }

    addOverlay(polygon, OverlayComparator)

    return polygon
}

fun MapView.addLandscapeOutline(
    overlayId: String,
    overlayType: OverlayType,
    way: GeometryUiModel.Way,
    @ColorInt landscapeColor: Int,
    onClick: (OverlayWithIW) -> Unit,
) {
    val context = this.context
    val geoPoints = way.geoPoints

    val polyline = if (overlayType == OverlayType.LANDSCAPE_DETAILS) {
        LandscapeDetailsPolyline(this)
    } else {
        LandscapePolyline(this)
    }

    polyline.apply {
        id = overlayId
        val borderPaint = Paint().apply {
            color = landscapeColor
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(landscapes_polyline_width)
            style = Style.STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
            isAntiAlias = true
        }
        this.outlinePaintLists.add(MonochromaticPaintList(borderPaint))
        setPoints(geoPoints)
        setOnClickListener { polygon, _, _ ->
            onClick.invoke(polygon)
            true
        }
    }

    addOverlay(polyline, OverlayComparator)
}

fun MapView.addDestinationMarker(
    overlayId: String,
    overlayType: OverlayType,
    geoPoint: GeoPoint,
    @ColorInt markerColor: Int,
    destinationType: DestinationType,
    infoWindowTitle: String,
    infoWindowDescription: String? = null,
    onMarkerClick: () -> Unit,
    onInfoWindowNavigationClick: (GeoPoint) -> Unit,
) {
    val destinationMarker = when (overlayType) {
        OverlayType.LANDSCAPE_DETAILS -> {
            LandscapeDetailsDestinationMarker(this)
        }
        OverlayType.LANDSCAPE_MAP -> {
            LandscapeMapDestinationMarker(this)
        }
        OverlayType.PLACE_DETAILS -> {
            PlaceDetailsDestinationMarker(this)
        }
        else -> return
    }

    val marker = destinationMarker.apply {
        id = overlayId
        position = geoPoint
        icon = generateLayerDrawable(
            listOf(
                LayerDrawableConfig(
                    R.drawable.ic_marker_background_stroke.toDrawable(context),
                    resources.getDimensionPixelSize(R.dimen.destination_marker_background_size),
                ),
                LayerDrawableConfig(
                    R.drawable.ic_marker_background.toDrawable(context, markerColor),
                    resources.getDimensionPixelSize(R.dimen.destination_marker_background_size),
                ),
                LayerDrawableConfig(
                    destinationType.resolveIcon().toDrawable(context, R.color.colorStrokeMarker.color(context)),
                    resources.getDimensionPixelSize(R.dimen.destination_marker_icon_size),
                ),
            ),
        )
        infoWindow = NavigationMarkerInfoWindow(
            mapView = this@addDestinationMarker,
            title = infoWindowTitle,
            description = infoWindowDescription,
            onNavigationClick = { onInfoWindowNavigationClick.invoke(geoPoint) },
        )
        setOnMarkerClickListener { marker, mapView ->
            onMarkerClick.invoke()

            marker.showInfoWindow()
            mapView.controller.animateTo(marker.position)

            true
        }
    }

    addOverlay(marker, OverlayComparator)
}

fun MapView.addGpxMarker(
    overlayId: String,
    geoPoint: GeoPoint,
    waypointType: WaypointType,
    infoWindowTitle: String? = null,
    infoWindowDescription: String? = null,
    onMarkerClick: (Marker) -> Unit,
    onWaypointClick: () -> Unit,
    onWaypointNavigationClick: (GeoPoint) -> Unit,
): Marker {
    val iconDrawable = when (waypointType) {
        WaypointType.START -> R.drawable.ic_marker_gpx_start.toDrawable(this.context)
        WaypointType.INTERMEDIATE -> R.drawable.ic_marker_gpx_intermediate.toDrawable(this.context)
        WaypointType.END -> R.drawable.ic_marker_gpx_end.toDrawable(this.context)
        WaypointType.ROUND_TRIP -> R.drawable.ic_marker_gpx_round_trip.toDrawable(this.context)
    }
    val marker = GpxMarker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        if (infoWindowTitle != null) {
            infoWindow = NavigationMarkerInfoWindow(
                mapView = this@addGpxMarker,
                title = infoWindowTitle,
                description = infoWindowDescription,
                onNavigationClick = { onWaypointNavigationClick.invoke(geoPoint) },
            )
            setOnMarkerClickListener { marker, mapView ->
                onWaypointClick.invoke()

                marker.showInfoWindow()
                mapView.controller.animateTo(marker.position)

                true
            }
        } else {
            setOnMarkerClickListener { marker, _ ->
                onMarkerClick.invoke(marker)
                true
            }
        }
        if (waypointType == WaypointType.START || waypointType == WaypointType.END) {
            infoWindow = DistanceInfoWindow(this@addGpxMarker)
        }
    }

    addOverlay(marker, OverlayComparator)

    return marker
}

fun MapView.addGpxPolyline(
    overlayId: String,
    geoPoints: List<GeoPoint>,
    arrowGeoPoints: List<Pair<GeoPoint, Int>>,
    useAltitudeColors: Boolean,
    onClick: (PolyOverlayWithIW) -> Unit
): Polyline {
    val context = this.context
    val polyline = GpxPolyline(this).apply {
        id = overlayId

        setPoints(geoPoints)

        val borderPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPolylineBorder)
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_gpx_width)
            style = Style.STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
            isAntiAlias = true
        }
        outlinePaintLists.add(MonochromaticPaintList(borderPaint))

        val fillPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_gpx_fill_width)
            style = Style.FILL_AND_STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.colorPolyline)
        }

        if (useAltitudeColors) {
            val gradientColors = getSlopeGradientColors(
                locations = geoPoints.toLocationsWithAlt(),
                midSlope = SLOPE_PERCENTAGE_MID.toDouble(),
                highSlope = SLOPE_PERCENTAGE_HIGH.toDouble(),
                negativeSlopeColorHigh = ContextCompat.getColor(context, R.color.colorSlopeNegativeHigh),
                negativeSlopeColorMid = ContextCompat.getColor(context, R.color.colorSlopeNegativeMid),
                zeroSlopeColor = ContextCompat.getColor(context, R.color.colorSlopeZero),
                positiveSlopeColorMid = ContextCompat.getColor(context, R.color.colorSlopePositiveMid),
                positiveSlopeColorHigh = ContextCompat.getColor(context, R.color.colorSlopePositiveHigh),
            )
            val colorMapping = ColorMappingCycle(gradientColors)
            outlinePaintLists.add(PolychromaticPaintList(fillPaint, colorMapping, false))
        } else {
            outlinePaintLists.add(MonochromaticPaintList(fillPaint))
        }

        setOnClickListener { polygon, _, _ ->
            onClick.invoke(polygon)
            true
        }
    }

    addOverlay(polyline, OverlayComparator)

    arrowGeoPoints.forEach { (geoPoint, bearing) ->
        val directionMarker = GpxArrowMarker(this@addGpxPolyline).apply {
            id = overlayId
            position = geoPoint
            icon = R.drawable.ic_gpx_direction_arrow.toDrawable(context)
            rotation = -bearing.toFloat()
            setAnchor(DIRECTION_ARROW_ICON_ANCHOR, DIRECTION_ARROW_ICON_ANCHOR)
            isFlingEnabled = false
            isFlat = true
            isClickable = false
            infoWindow = null
            setOnMarkerClickListener { _, _ ->
                onClick.invoke(polyline)
                true
            }
        }
        addOverlay(directionMarker, OverlayComparator)
    }

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
        WaypointType.ROUND_TRIP -> R.drawable.ic_marker_gpx_round_trip.toDrawable(this.context)
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
            strokeWidth = context.resources.getDimension(R.dimen.default_gpx_width)
            style = Style.STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
            isAntiAlias = true
        }
        outlinePaintLists.add(MonochromaticPaintList(borderPaint))
        val fillPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorPolyline)
            isAntiAlias = true
            strokeWidth = context.resources.getDimension(R.dimen.default_gpx_fill_width)
            style = Style.FILL_AND_STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
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
    closeInfoWindows<LocationPickerInfoWindow>()
    removeOverlay(OverlayType.LOCATION_PICKER)

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
            colorBorder = ContextCompat.getColor(context, R.color.colorStrokeMarkerDark),
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
    onRouteClick: () -> Unit,
    onWaypointClick: () -> Unit,
    onWaypointNavigationClick: (GeoPoint) -> Unit,
) {
    if (oktRouteUiModel.oktId != OKT_ID_FULL_ROUTE) {
        addOktPolyline(
            overlayId = overlayId,
            geoPoints = oktRouteUiModel.geoPoints,
            onClick = { onRouteClick.invoke() }
        )
    }

    oktRouteUiModel.stampWaypoints.forEach { stampWaypoint ->
        addOktMarker(
            overlayId = overlayId,
            geoPoint = stampWaypoint.location.toGeoPoint(),
            iconDrawable = generateLayerDrawable(
                layers = listOf(
                    LayerDrawableConfig(
                        R.drawable.ic_marker_okt_route_background.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.okt_routes_stamp_marker_background_size)
                    ),
                    LayerDrawableConfig(
                        R.drawable.ic_okt_stamp.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.okt_routes_marker_icon_size)
                    ),
                ),
            ),
            infoWindowTitle = stampWaypoint.title,
            infoWindowDescription = stampWaypoint.description,
            onMarkerClick = onWaypointClick,
            onInfoWindowNavigationClick = { onWaypointNavigationClick.invoke(stampWaypoint.location.toGeoPoint()) },
        )
    }

    listOf(oktRouteUiModel.start, oktRouteUiModel.end).forEach { geoPoint ->
        addOktMarker(
            overlayId = overlayId,
            geoPoint = geoPoint,
            iconDrawable = generateLayerDrawable(
                layers = listOf(
                    LayerDrawableConfig(
                        R.drawable.ic_marker_okt_route_background.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.okt_routes_marker_background_size)
                    ),
                    LayerDrawableConfig(
                        R.drawable.ic_marker_okt_routes.toDrawable(context),
                        resources.getDimensionPixelSize(R.dimen.okt_routes_marker_icon_size)
                    ),
                ),
            ),
            infoWindowTitle = oktRouteUiModel.routeName,
            onMarkerClick = onWaypointClick,
            onInfoWindowNavigationClick = { onWaypointNavigationClick.invoke(geoPoint) },
        )
    }
}

fun MapView.addOktMarker(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    infoWindowTitle: String,
    infoWindowDescription: String? = null,
    onMarkerClick: () -> Unit,
    onInfoWindowNavigationClick: (GeoPoint) -> Unit,
) {
    val marker = OktMarker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        infoWindow = NavigationMarkerInfoWindow(
            mapView = this@addOktMarker,
            title = infoWindowTitle,
            description = infoWindowDescription,
            onNavigationClick = { onInfoWindowNavigationClick.invoke(geoPoint) },
        )
        setOnMarkerClickListener { marker, mapView ->
            onMarkerClick.invoke()

            marker.showInfoWindow()
            mapView.controller.animateTo(marker.position)

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
            style = Style.STROKE
            strokeJoin = Join.ROUND
            strokeCap = Cap.ROUND
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

fun MapView.addPlaceCategoryMarker(
    overlayId: String = UUID.randomUUID().toString(),
    placeCategory: PlaceCategory,
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    infoWindowTitle: String? = null,
    infoWindowDescription: String? = null,
    onMarkerClick: (Marker) -> Unit,
) {
    if (this.hasOverlay(overlayId)) {
        return
    }

    val marker = PlaceCategoryMarker(this, placeCategory).apply {
        id = overlayId
        position = geoPoint
        icon = generateLayerDrawable(
            listOf(
                LayerDrawableConfig(
                    R.drawable.ic_marker_background_stroke.toDrawable(context),
                    resources.getDimensionPixelSize(R.dimen.place_category_marker_background_size),
                ),
                LayerDrawableConfig(
                    R.drawable.ic_marker_background.toDrawable(context, placeCategory.categoryColorRes?.color(context)),
                    resources.getDimensionPixelSize(R.dimen.place_category_marker_background_size),
                ),
                LayerDrawableConfig(
                    iconDrawable,
                    resources.getDimensionPixelSize(R.dimen.place_category_marker_icon_size),
                ),
            ),
        )
        setOnMarkerClickListener { marker, _ ->
            onMarkerClick.invoke(marker)
            true
        }
        if (infoWindowTitle != null) {
            infoWindow = NavigationMarkerInfoWindow(
                mapView = this@addPlaceCategoryMarker,
                title = infoWindowTitle,
                description = infoWindowDescription,
            )
        }
    }

    addOverlay(marker, OverlayComparator)
}

fun MapView.replaceScaleBarOverlay(insets: Insets) {
    val context = this.context
    val scaleBarOverlay = HukiScaleBarOverlay(this, R.color.colorMapOverlayStroke.color(context)).apply {
        setAlignBottom(true)
        setScaleBarOffset(
            context.resources.getDimensionPixelSize(R.dimen.space_large),
            context.resources.getDimensionPixelSize(R.dimen.space_extra_huge) + insets.bottom
        )
        setTextSize(context.resources.getDimensionPixelSize(R.dimen.text_size_extra_small).toFloat())
        setTextTypeFace(context.resources.getFont(R.font.opensans_bold))
        barPaint.apply {
            color = context.getColor(R.color.colorMapOverlayText)
        }
        textPaint.apply {
            color = context.getColor(R.color.colorMapOverlayText)
        }
    }

    replaceOverlay(scaleBarOverlay, OverlayComparator)
}

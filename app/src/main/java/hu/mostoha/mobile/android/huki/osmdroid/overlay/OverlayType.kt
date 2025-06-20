package hu.mostoha.mobile.android.huki.osmdroid.overlay

import hu.mostoha.mobile.android.huki.osmdroid.OsmLicencesOverlay
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

/**
 * Identifies the overlay type by its function.
 * Its ordinal represents the render order of the map.
 */
enum class OverlayType {
    HIKING_LAYER,
    LANDSCAPE,
    OKT_ROUTES_BASE,
    OKT_ROUTES,
    GPX,
    ROUTE_PLANNER,
    PLACE_DETAILS,
    MY_LOCATION,
    LICENCES,
    SCALE_BAR,
    ROTATION_GESTURE,
    MAP_TOUCH_EVENTS,
    LOCATION_PICKER,
}

/**
 * Map which associates the [OverlayType]s to the overlay components of the map.
 */
val OVERLAY_TYPE_ORDER_MAP = mapOf(
    OverlayType.LICENCES to listOf(OsmLicencesOverlay::class),
    OverlayType.SCALE_BAR to listOf(HukiScaleBarOverlay::class),
    OverlayType.ROTATION_GESTURE to listOf(RotationGestureOverlay::class),
    OverlayType.HIKING_LAYER to listOf(TilesOverlay::class),
    OverlayType.MY_LOCATION to listOf(MyLocationOverlay::class),
    OverlayType.LANDSCAPE to listOf(LandscapePolyline::class, LandscapePolygon::class),
    OverlayType.OKT_ROUTES_BASE to listOf(OktBasePolyline::class),
    OverlayType.OKT_ROUTES to listOf(OktMarker::class, OktPolyline::class),
    OverlayType.PLACE_DETAILS to listOf(
        Marker::class,
        PlaceDetailsMarker::class,
        PlaceCategoryMarker::class,
        Polyline::class,
        Polygon::class
    ),
    OverlayType.GPX to listOf(GpxMarker::class, GpxPolyline::class, GpxArrowMarker::class),
    OverlayType.ROUTE_PLANNER to listOf(RoutePlannerMarker::class, RoutePlannerPolyline::class),
    OverlayType.MAP_TOUCH_EVENTS to listOf(MapEventsOverlay::class),
    OverlayType.LOCATION_PICKER to listOf(LocationPickerMarker::class),
)

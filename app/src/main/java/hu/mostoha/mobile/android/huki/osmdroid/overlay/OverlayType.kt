package hu.mostoha.mobile.android.huki.osmdroid.overlay

import hu.mostoha.mobile.android.huki.osmdroid.OsmLicencesOverlay
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

/**
 * Identifies the overlay type by its function.
 * Its ordinal is used to define the render order of the map.
 */
enum class OverlayType {
    LICENCES,
    HIKING_LAYER,
    MY_LOCATION,
    PLACE_DETAILS,
    GPX
}

/**
 * Map which associates the [OverlayType]s to the overlay components of the map.
 */
val OVERLAY_TYPE_ORDER_MAP = mapOf(
    OverlayType.LICENCES to listOf(OsmLicencesOverlay::class),
    OverlayType.HIKING_LAYER to listOf(TilesOverlay::class),
    OverlayType.MY_LOCATION to listOf(MyLocationOverlay::class),
    OverlayType.PLACE_DETAILS to listOf(Marker::class, Polygon::class, Polyline::class),
    OverlayType.GPX to listOf(GpxMarker::class, GpxPolyline::class)
)

package hu.mostoha.mobile.android.huki.ui.home

import hu.mostoha.mobile.android.huki.osmdroid.OsmCopyrightOverlay
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

/**
 * A comparator which ensures the order of the map view components rendered by its function.
 *      1. Copyright
 *      2. Hiking Layer
 *      3. My location
 *      4. Place details (Markers, Polylines, Polygons...)
 */
object OverlayComparator : Comparator<Overlay> {

    private enum class OverlayType {
        COPYRIGHT,
        HIKING_LAYER,
        MY_LOCATION,
        PLACE_DETAILS
    }

    private val overlayOrderMap = mapOf(
        listOf(OsmCopyrightOverlay::class) to OverlayType.COPYRIGHT,
        listOf(TilesOverlay::class) to OverlayType.HIKING_LAYER,
        listOf(MyLocationOverlay::class) to OverlayType.MY_LOCATION,
        listOf(Marker::class, Polygon::class, Polyline::class) to OverlayType.PLACE_DETAILS
    )

    override fun compare(overlay1: Overlay, overlay2: Overlay): Int {
        val overlay1Types = overlayOrderMap.keys.first { it.contains(overlay1::class) }
        val overlay2Types = overlayOrderMap.keys.first { it.contains(overlay2::class) }

        val overlayOrder1 = overlayOrderMap.getValue(overlay1Types).ordinal
        val overlayOrder2 = overlayOrderMap.getValue(overlay2Types).ordinal

        return overlayOrder1.compareTo(overlayOrder2)
    }

}

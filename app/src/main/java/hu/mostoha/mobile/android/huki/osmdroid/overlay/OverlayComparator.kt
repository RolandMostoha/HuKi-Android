package hu.mostoha.mobile.android.huki.osmdroid.overlay

import org.osmdroid.views.overlay.Overlay

/**
 * A comparator which ensures the order of the map view components rendered by its function.
 */
object OverlayComparator : Comparator<Overlay> {

    override fun compare(overlay1: Overlay, overlay2: Overlay): Int {
        val overlay1Type = OVERLAY_TYPE_ORDER_MAP.filterValues { it.contains(overlay1::class) }.keys.first()
        val overlay2Type = OVERLAY_TYPE_ORDER_MAP.filterValues { it.contains(overlay2::class) }.keys.first()

        val overlayOrder1 = overlay1Type.ordinal
        val overlayOrder2 = overlay2Type.ordinal

        return overlayOrder1.compareTo(overlayOrder2)
    }

}

package hu.mostoha.mobile.android.huki.osmdroid.overlay

import android.graphics.drawable.Drawable
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Marker class to differentiate OKT routes related [Marker] classes.
 */
class OktMarker(private val mapView: MapView) : InfoWindowMarker(mapView) {

    fun updateIcon(drawable: Drawable) {
        icon = drawable
        mapView.invalidate()
    }

}

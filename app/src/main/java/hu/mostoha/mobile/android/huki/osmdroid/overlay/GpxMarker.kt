package hu.mostoha.mobile.android.huki.osmdroid.overlay

import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

/**
 * Marker class to differentiate GPX related [Marker] classes.
 */
class GpxMarker(mapView: MapView) : Marker(mapView) {

    override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
        InfoWindow.closeAllInfoWindowsOn(mapView)

        return super.onSingleTapConfirmed(event, mapView)

    }
}

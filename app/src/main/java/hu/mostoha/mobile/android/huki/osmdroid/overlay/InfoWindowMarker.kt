package hu.mostoha.mobile.android.huki.osmdroid.overlay

import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

/**
 * Marker class which closes the corresponding [InfoWindow] automatically on map or marker tap.
 */
open class InfoWindowMarker(mapView: MapView) : Marker(mapView) {

    override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
        InfoWindow.closeAllInfoWindowsOn(mapView)

        return super.onSingleTapConfirmed(event, mapView)
    }

}

package hu.mostoha.mobile.android.huki.osmdroid.overlay

import android.view.MotionEvent
import hu.mostoha.mobile.android.huki.extensions.closeInfoWindows
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.GpxMarkerInfoWindow
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

/**
 * Marker class which closes the corresponding [InfoWindow] automatically on map or marker tap.
 */
open class InfoWindowMarker(mapView: MapView) : Marker(mapView) {

    override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
        mapView?.closeInfoWindows<GpxMarkerInfoWindow>()

        return super.onSingleTapConfirmed(event, mapView)
    }

}

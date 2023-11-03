package hu.mostoha.mobile.android.huki.osmdroid.overlay

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Marker class to differentiate GPX related [Marker] classes.
 */
class GpxMarker(mapView: MapView) : InfoWindowMarker(mapView)

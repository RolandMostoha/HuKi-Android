package hu.mostoha.mobile.android.huki.osmdroid.overlay

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Marker class to differentiate destination related [Marker] classes.
 */
open class DestinationMarker(mapView: MapView) : InfoWindowMarker(mapView)

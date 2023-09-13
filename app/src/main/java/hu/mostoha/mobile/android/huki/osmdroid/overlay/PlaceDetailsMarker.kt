package hu.mostoha.mobile.android.huki.osmdroid.overlay

import hu.mostoha.mobile.android.huki.model.ui.Message
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Marker class to differentiate Place Details related [Marker] classes.
 */
class PlaceDetailsMarker(mapView: MapView, val name: Message) : Marker(mapView)

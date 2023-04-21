package hu.mostoha.mobile.android.huki.osmdroid.overlay

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Marker class to differentiate Landscape related [Polygon] classes.
 */
class LandscapePolygon(mapView: MapView) : Polygon(mapView)

package hu.mostoha.mobile.android.huki.osmdroid.overlay

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

/**
 * Marker class to differentiate route planner related [Polyline] classes.
 */
class RoutePlannerPolyline(mapView: MapView) : Polyline(mapView)

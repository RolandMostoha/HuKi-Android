package hu.mostoha.mobile.android.huki.osmdroid.overlay

import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Marker class to differentiate Place Category [Marker] classes.
 */
class PlaceCategoryMarker(mapView: MapView, val placeCategory: PlaceCategory) : InfoWindowMarker(mapView)

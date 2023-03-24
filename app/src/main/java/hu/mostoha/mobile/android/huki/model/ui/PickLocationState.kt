package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

sealed class PickLocationState {

    object Started : PickLocationState()

    data class LocationPicked(val geoPoint: GeoPoint) : PickLocationState()

}

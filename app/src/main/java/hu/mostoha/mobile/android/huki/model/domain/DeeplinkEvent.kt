package hu.mostoha.mobile.android.huki.model.domain

sealed class DeeplinkEvent {

    data class LandscapeDetails(val osmId: String) : DeeplinkEvent()

    data class PlaceDetails(val lat: Double, val lon: Double) : DeeplinkEvent()

}

package hu.mostoha.mobile.android.huki.model.domain

data class PlaceProfile(
    val osmId: String,
    val address: PlaceAddress,
    val placeType: PlaceType,
    val location: Location
)

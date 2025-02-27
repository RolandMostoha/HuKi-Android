package hu.mostoha.mobile.android.huki.model.domain

data class PlaceProfile(
    val osmId: String,
    val placeType: PlaceType,
    val location: Location,
    val displayName: String,
    val displayAddress: String,
    val address: PlaceAddress,
    val boundingBox: BoundingBox? = null,
)

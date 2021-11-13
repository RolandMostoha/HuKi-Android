package hu.mostoha.mobile.android.huki.model.domain

data class Place(
    val osmId: String,
    val name: String,
    val placeType: PlaceType,
    val location: Location,
    val boundingBox: BoundingBox? = null,
    val country: String? = null,
    val county: String? = null,
    val district: String? = null,
    val postCode: String? = null,
    val city: String? = null,
    val street: String? = null
)

package hu.mostoha.mobile.android.huki.model.domain

data class PlacePrediction(
    val id: String,
    val placeType: PlaceType,
    val primaryText: String,
    val secondaryText: String?
)
package hu.mostoha.mobile.android.huki.model.domain

data class Place(
    val osmId: String,
    val name: String,
    val placeType: PlaceType,
    val location: Location,
    val address: String,
    val placeFeature: PlaceFeature,
    val historyInfo: HistoryInfo? = null,
    val boundingBox: BoundingBox? = null,
)

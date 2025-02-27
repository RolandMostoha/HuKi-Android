package hu.mostoha.mobile.android.huki.model.domain

import hu.mostoha.mobile.android.huki.model.ui.Message

data class Place(
    val osmId: String,
    val name: Message,
    val fullAddress: String,
    val placeType: PlaceType,
    val location: Location,
    val placeFeature: PlaceFeature,
    val osmTags: Map<String, String>? = null,
    val historyInfo: HistoryInfo? = null,
    val boundingBox: BoundingBox? = null,
    val placeCategory: PlaceCategory? = null
)

package hu.mostoha.mobile.android.turistautak.model.ui

import org.osmdroid.util.GeoPoint

data class PlaceDetailsUiModel(
    val id: String,
    val payLoad: UiPayLoad
)

sealed class UiPayLoad {
    data class Node(val geoPoint: GeoPoint) : UiPayLoad()
    data class Way(val geoPoints: List<GeoPoint>) : UiPayLoad()
    data class Relation(val geoPoints: List<GeoPoint>) : UiPayLoad()
}

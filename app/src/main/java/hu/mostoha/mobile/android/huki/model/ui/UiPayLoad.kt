package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

sealed class UiPayLoad {
    data class Node(val geoPoint: GeoPoint) : UiPayLoad()
    data class Way(val id: String, val geoPoints: List<GeoPoint>, val isClosed: Boolean) : UiPayLoad()
    data class Relation(val ways: List<Way>) : UiPayLoad()
}

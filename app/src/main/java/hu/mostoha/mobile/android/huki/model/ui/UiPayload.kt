package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

sealed class UiPayload {

    data class Node(val geoPoint: GeoPoint) : UiPayload()

    data class Way(val osmId: String, val geoPoints: List<GeoPoint>, val isClosed: Boolean) : UiPayload()

    data class Relation(val ways: List<Way>) : UiPayload()

}

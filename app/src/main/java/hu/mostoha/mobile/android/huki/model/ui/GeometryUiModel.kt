package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

sealed class GeometryUiModel {

    data class Node(val geoPoint: GeoPoint) : GeometryUiModel()

    data class Way(val osmId: String, val geoPoints: List<GeoPoint>, val isClosed: Boolean) : GeometryUiModel()

    data class Relation(val ways: List<Way>) : GeometryUiModel()

}

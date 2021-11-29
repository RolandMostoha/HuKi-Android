package hu.mostoha.mobile.android.huki.model.domain

sealed class Geometry {

    data class Node(val osmId: String, val location: Location) : Geometry()

    data class Way(val osmId: String, val locations: List<Location>, val distance: Int) : Geometry()

    data class Relation(val osmId: String, val ways: List<Way>) : Geometry()

}

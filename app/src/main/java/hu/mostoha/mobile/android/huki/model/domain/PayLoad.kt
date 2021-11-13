package hu.mostoha.mobile.android.huki.model.domain

sealed class Payload {

    data class Node(val location: Location) : Payload()

    data class Way(val osmId: String, val locations: List<Location>, val distance: Int) : Payload()

    data class Relation(val ways: List<Way>) : Payload()

}

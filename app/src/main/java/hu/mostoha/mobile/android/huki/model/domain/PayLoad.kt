package hu.mostoha.mobile.android.huki.model.domain

sealed class PayLoad {
    data class Node(val location: Location) : PayLoad()
    data class Way(val id: String, val locations: List<Location>, val distance: Int) : PayLoad()
    data class Relation(val ways: List<Way>) : PayLoad()
}
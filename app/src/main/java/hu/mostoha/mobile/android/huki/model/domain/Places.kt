package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.util.GeoPoint

data class PlacePrediction(
    val id: String,
    val placeType: PlaceType,
    val primaryText: String,
    val secondaryText: String?
)

enum class PlaceType {
    NODE,
    WAY,
    RELATION
}

data class PlaceDetails(
    val id: String,
    val payLoad: PayLoad
)

sealed class PayLoad {
    data class Node(val location: Location) : PayLoad()
    data class Way(val id: String, val locations: List<Location>, val distance: Int) : PayLoad()
    data class Relation(val ways: List<Way>) : PayLoad()
}

data class Location(
    val latitude: Double,
    val longitude: Double
)

fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

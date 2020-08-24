package hu.mostoha.mobile.android.turistautak.model.domain

import com.google.android.gms.maps.model.LatLng
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
    data class Way(val locations: List<Location>) : PayLoad()
}

data class Location(
    val latitude: Double,
    val longitude: Double
)

fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

fun LatLng.toLocation() = Location(latitude, longitude)
package hu.mostoha.mobile.android.turistautak.model.domain

import com.google.android.gms.maps.model.LatLng
import org.osmdroid.util.GeoPoint

data class PlacePrediction(
    val id: String,
    val primaryText: String,
    val secondaryText: String?
)

data class PlaceDetails(
    val id: String,
    val coordinates: Coordinates
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

fun Coordinates.toGeoPoint() = GeoPoint(latitude, longitude)

fun LatLng.toCoordinates() = Coordinates(latitude, longitude)
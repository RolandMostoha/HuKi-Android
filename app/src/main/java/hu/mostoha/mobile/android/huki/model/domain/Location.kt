package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.util.GeoPoint

data class Location(
    val latitude: Double,
    val longitude: Double
)

fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

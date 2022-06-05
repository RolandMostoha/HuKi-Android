package hu.mostoha.mobile.android.huki.model.domain

import android.location.LocationManager
import org.osmdroid.util.GeoPoint
import android.location.Location as AndroidLocation

data class Location(
    val latitude: Double,
    val longitude: Double
)

fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

fun Location.toAndroidLocation() = AndroidLocation(LocationManager.GPS_PROVIDER).apply {
    latitude = this.latitude
    longitude = this.longitude
}

fun GeoPoint.toLocation() = Location(latitude, longitude)

fun AndroidLocation.toLocation() = Location(latitude, longitude)

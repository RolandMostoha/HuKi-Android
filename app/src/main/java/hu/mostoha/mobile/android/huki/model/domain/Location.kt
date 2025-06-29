package hu.mostoha.mobile.android.huki.model.domain

import android.location.LocationManager
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import android.location.Location as AndroidLocation

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null
)

fun Location.toGeoPoint() = GeoPoint(latitude, longitude, altitude ?: 0.0)

fun Location.equalsWithoutAlt(other: Location): Boolean {
    return this.latitude == other.latitude && this.longitude == other.longitude
}

fun List<Location>.toGeoPoints() = this.map { it.toGeoPoint() }

fun Location.toAndroidLocation() = AndroidLocation(LocationManager.GPS_PROVIDER).apply {
    latitude = this.latitude
    longitude = this.longitude
}

fun IGeoPoint.toLocation() = Location(latitude, longitude)

fun GeoPoint.toLocationWithAlt() = Location(latitude, longitude, altitude)

fun List<GeoPoint>.toLocations() = this.map { it.toLocation() }

fun List<GeoPoint>.toLocationsWithAlt() = this.map { it.toLocationWithAlt() }

fun AndroidLocation.toLocation() = Location(latitude, longitude)

fun List<Double>.toLocation() = Location(this[1], this[0], this[2])

fun List<List<Double>>.toLocationsFromDoubles(): List<Location> {
    return map { it.toLocation() }
}

fun WayPoint.toLocation() = Location(this.latitude, this.longitude, this.elevation)

fun Triple<Double, Double, Double>.toLocation() = Location(first, second, third)

fun List<Triple<Double, Double, Double>>.toLocationsTriple() = this.map { it.toLocation() }

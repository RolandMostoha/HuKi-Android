package hu.mostoha.mobile.android.huki.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.BoundingBox as OsmBoundingBox

@Parcelize
data class BoundingBox(
    val north: Double, // maxLat
    val east: Double, // maxLon
    val south: Double, // minLat
    val west: Double // minLon
) : Parcelable {
    override fun toString(): String {
        return "north: %.5f, east: %.5f, south: %.5f, west: %.5f".format(north, east, south, west)
    }
}

fun BoundingBox.toViewBox(): String {
    return "$east,$north,$west,$south"
}

fun BoundingBox.toOsm() = OsmBoundingBox(north, east, south, west)

fun OsmBoundingBox.toDomain() = BoundingBox(latNorth, lonEast, latSouth, lonWest)

fun BoundingBox.center(): Location {
    val centerLat = (south + north) / 2
    val centerLon = (west + east) / 2

    return Location(centerLat, centerLon)
}

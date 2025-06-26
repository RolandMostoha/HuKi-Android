package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location

@JsonClass(generateAdapter = true)
data class Bounds(
    @Json(name = "minlat")
    var minLat: Double,

    @Json(name = "minlon")
    var minLon: Double,

    @Json(name = "maxlat")
    var maxLat: Double,

    @Json(name = "maxlon")
    var maxLon: Double
)

fun Bounds.center(): Location {
    val centerLat = (minLat + maxLat) / 2
    val centerLon = (minLon + maxLon) / 2

    return Location(centerLat, centerLon)
}

fun Bounds.toBoundingBox(): BoundingBox {
    return BoundingBox(maxLat, maxLon, minLat, minLon)
}

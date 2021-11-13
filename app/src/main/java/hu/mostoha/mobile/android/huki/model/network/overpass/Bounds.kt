package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Bounds(
    @field:Json(name = "minlat")
    var minLat: Double,

    @field:Json(name = "minlon")
    var minLon: Double,

    @field:Json(name = "maxlat")
    var maxLat: Double,

    @field:Json(name = "maxlon")
    var maxLon: Double
)

package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Geom(
    @field:Json(name = "lat")
    var lat: Double? = null,

    @field:Json(name = "lon")
    var lon: Double? = null
)

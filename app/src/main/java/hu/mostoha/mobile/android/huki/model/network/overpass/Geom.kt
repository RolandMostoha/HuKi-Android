package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Geom(
    @Json(name = "lat")
    var lat: Double? = null,

    @Json(name = "lon")
    var lon: Double? = null
)

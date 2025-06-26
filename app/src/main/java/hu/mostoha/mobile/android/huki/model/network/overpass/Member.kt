package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Member(
    @Json(name = "ref")
    var ref: String,

    @Json(name = "type")
    var type: ElementType,

    @Json(name = "geometry")
    var geometry: List<Geom>? = null
)

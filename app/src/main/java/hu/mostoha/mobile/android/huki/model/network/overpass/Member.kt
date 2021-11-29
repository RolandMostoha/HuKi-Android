package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Member(
    @field:Json(name = "ref")
    var ref: String,

    @field:Json(name = "type")
    var type: ElementType,

    @field:Json(name = "geometry")
    var geometry: List<Geom>? = null
)

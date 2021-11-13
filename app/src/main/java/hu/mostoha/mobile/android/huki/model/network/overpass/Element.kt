package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Element(
    @field:Json(name = "type")
    var type: ElementType,

    @field:Json(name = "id")
    var id: Long,

    @field:Json(name = "tags")
    var tags: Tags? = null,

    @field:Json(name = "lat")
    var lat: Double? = null,

    @field:Json(name = "lon")
    var lon: Double? = null,

    @field:Json(name = "geometry")
    var geometry: List<Geom>? = null,

    @field:Json(name = "members")
    var members: List<Member>? = null,

    @field:Json(name = "bounds")
    var bounds: Bounds? = null
)

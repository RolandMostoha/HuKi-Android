package hu.mostoha.mobile.android.huki.model.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OverpassQueryResponse(
    @field:Json(name = "elements")
    var elements: List<Element>
)

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
    var members: List<Member>? = null
)

enum class ElementType {
    @Json(name = "relation")
    RELATION,

    @Json(name = "way")
    WAY,

    @Json(name = "node")
    NODE
}

@JsonClass(generateAdapter = true)
data class Tags(
    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "name:hu")
    var nameHungarian: String? = null,

    @field:Json(name = "region:type")
    var regionType: String? = null,

    @field:Json(name = "jel")
    var jel: SymbolType? = null,

    @field:Json(name = "network")
    var network: String? = null,

    @field:Json(name = "operator")
    var operator: String? = null,

    @field:Json(name = "ref")
    var ref: String? = null,

    @field:Json(name = "type")
    var type: String? = null,

    @field:Json(name = "amenity")
    var amenity: String? = null,

    @field:Json(name = "website")
    var website: String? = null
)

@JsonClass(generateAdapter = true)
data class Geom(
    @field:Json(name = "lat")
    var lat: Double? = null,

    @field:Json(name = "lon")
    var lon: Double? = null
)

@JsonClass(generateAdapter = true)
data class Member(
    @field:Json(name = "ref")
    var ref: String? = null,

    @field:Json(name = "type")
    var type: ElementType? = null,

    @field:Json(name = "geometry")
    var geometry: List<Geom>? = null
)

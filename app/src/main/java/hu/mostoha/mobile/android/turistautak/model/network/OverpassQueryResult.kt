package hu.mostoha.mobile.android.turistautak.model.network

import com.squareup.moshi.Json

data class OverpassQueryResult(
    @field:Json(name = "elements")
    var elements: List<Element>
)

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
    var lon: Double? = null
)

enum class ElementType {
    @Json(name = "relation")
    RELATION,
    @Json(name = "way")
    WAY,
    @Json(name = "node")
    NODE
}

data class Tags(
    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "jel")
    var jel: String? = null,

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
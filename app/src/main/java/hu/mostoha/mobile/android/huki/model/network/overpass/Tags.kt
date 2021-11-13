package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
    var website: String? = null,

    @field:Json(name = "natural")
    var natural: String? = null
)

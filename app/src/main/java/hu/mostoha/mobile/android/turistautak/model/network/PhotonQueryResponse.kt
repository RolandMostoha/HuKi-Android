package hu.mostoha.mobile.android.turistautak.model.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotonQueryResponse(
    @Json(name = "features")
    val features: List<FeaturesItem>,
    @Json(name = "type")
    val type: String
)

@JsonClass(generateAdapter = true)
data class FeaturesItem(
    @Json(name = "geometry")
    val geometry: Geometry,

    @Json(name = "type")
    val type: String,

    @Json(name = "properties")
    val properties: Properties
)

@JsonClass(generateAdapter = true)
data class Geometry(
    @Json(name = "coordinates")
    val coordinates: List<Double>,

    @Json(name = "type")
    val type: String
)

@JsonClass(generateAdapter = true)
data class Properties(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "osm_id")
    val osmId: Long,

    @Json(name = "osm_type")
    val osmType: OsmType,

    @Json(name = "extent")
    val extent: List<Double>? = null,

    @Json(name = "country")
    val country: String? = null,

    @Json(name = "city")
    val city: String? = null,

    @Json(name = "osm_key")
    val osmKey: String,
    @Json(name = "osm_value")
    val osmValue: String,

    @Json(name = "postcode")
    val postcode: String? = null,

    @Json(name = "state")
    val state: String? = null,

    @Json(name = "street")
    val street: String? = null,

    @Json(name = "housenumber")
    val housenumber: String? = null
)

enum class OsmType {
    @Json(name = "R")
    RELATION,

    @Json(name = "W")
    WAY,

    @Json(name = "N")
    NODE
}


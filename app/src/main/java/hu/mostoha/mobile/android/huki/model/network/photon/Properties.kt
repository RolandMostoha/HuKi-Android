package hu.mostoha.mobile.android.huki.model.network.photon

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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

    @Json(name = "city")
    val city: String? = null,

    @Json(name = "osm_key")
    val osmKey: String,

    @Json(name = "osm_value")
    val osmValue: String,

    @Json(name = "country")
    val country: String? = null,

    @Json(name = "postcode")
    val postCode: String? = null,

    @Json(name = "state")
    val state: String? = null,

    @Json(name = "county")
    val county: String? = null,

    @Json(name = "street")
    val street: String? = null,

    @Json(name = "housenumber")
    val houseNumber: String? = null,

    @Json(name = "district")
    val district: String? = null
)

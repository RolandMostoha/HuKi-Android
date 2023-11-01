package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hit(

    @Json(name = "osm_id")
    val osmId: String,

    @Json(name = "osm_key")
    val osmKey: String,

    @Json(name = "osm_type")
    val osmType: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "country")
    val country: String,

    @Json(name = "city")
    val city: String?,

    @Json(name = "osm_value")
    val osmValue: String,

    @Json(name = "point")
    val point: Point,

    @Json(name = "extent")
    val extent: List<Double>?,

    @Json(name = "housenumber")
    val houseNumber: String?,

    @Json(name = "postcode")
    val postcode: String?,

    @Json(name = "street")
    val street: String?

)

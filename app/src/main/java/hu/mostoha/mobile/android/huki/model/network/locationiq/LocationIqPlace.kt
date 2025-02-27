package hu.mostoha.mobile.android.huki.model.network.locationiq

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationIqPlace(
    @Json(name = "place_id")
    val placeId: String,

    @Json(name = "osm_id")
    val osmId: String,

    @Json(name = "osm_type")
    val osmType: String,

    @Json(name = "licence")
    val licence: String,

    @Json(name = "lat")
    val lat: Double,

    @Json(name = "lon")
    val lon: Double,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "display_place")
    val displayPlace: String? = null,

    @Json(name = "display_address")
    val displayAddress: String? = null,

    @Json(name = "boundingbox")
    val boundingBox: List<Double>? = null,

    @Json(name = "class")
    val classType: String? = null,

    @Json(name = "type")
    val type: String? = null,

    @Json(name = "address")
    val address: LocationIqAddress? = null,
)

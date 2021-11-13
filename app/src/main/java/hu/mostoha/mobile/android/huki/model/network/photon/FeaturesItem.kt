package hu.mostoha.mobile.android.huki.model.network.photon

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeaturesItem(
    @Json(name = "geometry")
    val geometry: Geometry,

    @Json(name = "type")
    val type: String,

    @Json(name = "properties")
    val properties: Properties
)

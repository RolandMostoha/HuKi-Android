package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingRequest(

    @Json(name = "reverse")
    val reverse: Boolean,

    /**
     * The location in the format 'latitude,longitude' e.g. point=45.93272,11.58803.
     */
    @Json(name = "point")
    val point: String,

    @Json(name = "q")
    val q: String? = null

)

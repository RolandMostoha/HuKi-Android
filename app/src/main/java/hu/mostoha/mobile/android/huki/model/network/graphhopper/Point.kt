package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Point(

    @Json(name = "lat")
    val lat: Double,

    @Json(name = "lng")
    val lng: Double

)

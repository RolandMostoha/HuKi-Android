package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteResponse(

    @Json(name = "hints")
    val hints: Hints,

    @Json(name = "info")
    val info: Info,

    @Json(name = "paths")
    val paths: List<Path>

)

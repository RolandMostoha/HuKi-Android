package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteRequest(

    @field:Json(name = "profile")
    val profile: String,

    @field:Json(name = "points_encoded")
    val pointsEncoded: Boolean,

    @field:Json(name = "elevation")
    val elevation: Boolean,

    @field:Json(name = "instructions")
    val instructions: Boolean,

    @field:Json(name = "points")
    val points: List<List<Double>>,

    @field:Json(name = "algorithm")
    val algorithm: Algorithm? = null,

    @field:Json(name = "custom_model")
    val customModel: CustomModel? = null,

    @field:Json(name = "ch.disable")
    val chDisabled: Boolean? = null,

    )

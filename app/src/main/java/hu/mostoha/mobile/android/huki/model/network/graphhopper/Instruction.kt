package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Instruction(

    @Json(name = "distance")
    val distance: Double,

    @Json(name = "heading")
    val heading: Double,

    @Json(name = "interval")
    val interval: List<Int>,

    @Json(name = "last_heading")
    val lastHeading: Double,

    @Json(name = "sign")
    val sign: Int,

    @Json(name = "street_name")
    val streetName: String,

    @Json(name = "text")
    val text: String,

    @Json(name = "time")
    val time: Int

)

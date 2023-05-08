package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Priority(

    @Json(name = "if")
    val ifCondition: String,

    @Json(name = "multiply_by")
    val multiplyBy: String

)

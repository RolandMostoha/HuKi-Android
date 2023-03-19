package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Info(

    @Json(name = "copyrights")
    val copyrights: List<String>,

    @Json(name = "took")
    val took: Int

)

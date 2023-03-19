package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hints(

    @Json(name = "visited_nodes.average")
    val visitedNodesAverage: Double,

    @Json(name = "visited_nodes.sum")
    val visitedNodesSum: Double

)

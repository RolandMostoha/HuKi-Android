package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Path(

    @Json(name = "ascend")
    val ascend: Double,

    @Json(name = "bbox")
    val boundingBox: List<Double>,

    @Json(name = "descend")
    val descend: Double,

    @Json(name = "distance")
    val distance: Double,

    @Json(name = "points")
    val points: Points,

    @Json(name = "points_encoded")
    val pointsEncoded: Boolean,

    @Json(name = "snapped_waypoints")
    val snappedWaypoints: SnappedWaypoints,

    @Json(name = "time")
    val time: Int,

    @Json(name = "transfers")
    val transfers: Int,

    @Json(name = "weight")
    val weight: Double,

    @Json(name = "instructions")
    val instructions: List<Instruction>? = null

)

package hu.mostoha.mobile.android.huki.model.domain

import java.util.UUID
import kotlin.time.Duration

data class GpxDetails(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val fileUri: String,
    val locations: List<Location>,
    val gpxWaypoints: List<GpxWaypoint>,
    val travelTime: Duration,
    val distance: Int,
    val altitudeRange: Pair<Int, Int>,
    val incline: Int,
    val decline: Int
)

package hu.mostoha.mobile.android.huki.model.domain

import java.util.UUID
import kotlin.time.Duration

data class GpxDetails(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val locations: List<Location>,
    val travelTime: Duration,
    val distance: Int,
    val altitudeRange: Pair<Int, Int>,
    val incline: Int,
    val decline: Int,
    val isClosed: Boolean
)

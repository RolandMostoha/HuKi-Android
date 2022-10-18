package hu.mostoha.mobile.android.huki.model.domain

import java.util.UUID

data class GpxDetails(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val locations: List<Location>,
    val distance: Int,
    val altitudeRange: Pair<Int, Int>,
    val incline: Int,
    val decline: Int,
    val isClosed: Boolean
)

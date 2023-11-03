package hu.mostoha.mobile.android.huki.model.domain

import kotlin.time.Duration

data class OktRoute(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val incline: Int,
    val decline: Int,
    val travelTime: Duration,
    val start: Location,
    val end: Location,
    val stampTagsRange: ClosedFloatingPointRange<Double>,
)

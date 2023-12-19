package hu.mostoha.mobile.android.huki.model.domain

import android.net.Uri
import java.time.LocalDateTime
import kotlin.time.Duration

data class GpxHistory(
    val routePlannerGpxList: List<GpxHistoryItem>,
    val externalGpxList: List<GpxHistoryItem>,
)

data class GpxHistoryItem(
    val name: String,
    val fileUri: Uri,
    val lastModified: LocalDateTime,
    val waypointCount: Int,
    val travelTime: Duration,
    val distance: Int,
    val incline: Int,
    val decline: Int
)

package hu.mostoha.mobile.android.huki.model.domain

import android.net.Uri
import java.time.LocalDateTime

data class GpxHistory(
    val routePlannerGpxList: List<GpxHistoryItem>,
    val externalGpxList: List<GpxHistoryItem>,
)

data class GpxHistoryItem(
    val name: String,
    val fileUri: Uri,
    val lastModified: LocalDateTime,
)

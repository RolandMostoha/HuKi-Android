package hu.mostoha.mobile.android.huki.model.domain

import java.time.LocalDateTime

data class HistoryInfo(
    val isFavourite: Boolean,
    val storeDateTime: LocalDateTime,
)

package hu.mostoha.mobile.android.turistautak.model.domain

sealed class DownloadState {
    data class Started(val requestId: Long) : DownloadState()
    object InProgress : DownloadState()
    object Finished : DownloadState()
}
package hu.mostoha.mobile.android.huki.model.ui

import java.io.File

sealed class HikingLayerState {

    object NotDownloaded : HikingLayerState()

    object Downloading : HikingLayerState()

    data class Downloaded(
        val hikingLayerFile: File,
        val lastUpdatedText: String
    ) : HikingLayerState()

}

package hu.mostoha.mobile.android.huki.model.ui

import java.io.File

sealed class HikingLayerUiModel {

    object Loading : HikingLayerUiModel()

    object Downloading : HikingLayerUiModel()

    object NotDownloaded : HikingLayerUiModel()

    data class Downloaded(
        val hikingLayerFile: File,
        val lastUpdatedText: String
    ) : HikingLayerUiModel()

}

package hu.mostoha.mobile.android.huki.model.ui

import java.io.File

data class HikingLayerDetailsUiModel(
    val isHikingLayerFileDownloaded: Boolean,
    val hikingLayerFile: File?,
    val lastUpdatedText: String?
)

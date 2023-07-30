package hu.mostoha.mobile.android.huki.model.ui

import android.net.Uri

data class GpxRenameResult(
    val gpxUri: Uri,
    val newName: String,
)

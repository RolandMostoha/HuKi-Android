package hu.mostoha.mobile.android.huki.ui.home.history.gpx.rename

import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult

sealed class GpxRenameEvents {

    data class ValidationSuccess(val gpxRenameResult: GpxRenameResult) : GpxRenameEvents()

}

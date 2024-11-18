package hu.mostoha.mobile.android.huki.model.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class CompassState : Parcelable {

    @Parcelize
    data object North : CompassState()

    @Parcelize
    data object Live : CompassState()

    @Parcelize
    data class Free(val mapOrientation: Float) : CompassState()

}

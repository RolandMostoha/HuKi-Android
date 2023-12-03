package hu.mostoha.mobile.android.huki.model.ui

sealed class CompassState {

    object North : CompassState()

    object Live : CompassState()

    data class Free(val mapOrientation: Float) : CompassState()

}

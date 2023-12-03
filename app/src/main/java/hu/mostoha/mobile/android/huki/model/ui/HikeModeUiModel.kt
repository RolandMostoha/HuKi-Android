package hu.mostoha.mobile.android.huki.model.ui

data class HikeModeUiModel(
    val isHikeModeEnabled: Boolean = false,
    val compassState: CompassState = CompassState.North,
)

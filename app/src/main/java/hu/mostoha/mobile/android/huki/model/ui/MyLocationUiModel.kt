package hu.mostoha.mobile.android.huki.model.ui

data class MyLocationUiModel(
    val isLocationPermissionEnabled: Boolean = false,
    val isFollowLocationEnabled: Boolean = true,
    val isZoomLocked: Boolean = false,
)

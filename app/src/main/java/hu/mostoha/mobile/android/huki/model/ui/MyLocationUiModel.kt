package hu.mostoha.mobile.android.huki.model.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyLocationUiModel(
    val isLocationPermissionEnabled: Boolean = false,
    val isFollowLocationEnabled: Boolean = true
) : Parcelable

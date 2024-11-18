package hu.mostoha.mobile.android.huki.model.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HikeModeUiModel(
    val isHikeModeEnabled: Boolean = false,
    val compassState: CompassState = CompassState.North,
) : Parcelable

package hu.mostoha.mobile.android.huki.model.ui

import android.os.Parcelable
import hu.mostoha.mobile.android.huki.model.domain.OktType
import kotlinx.parcelize.Parcelize

@Parcelize
data class OktRoutesSavedStateUiModel(
    val oktType: OktType,
    val selectedOktId: String,
    val isStarted: Boolean,
    val isReversed: Boolean,
) : Parcelable

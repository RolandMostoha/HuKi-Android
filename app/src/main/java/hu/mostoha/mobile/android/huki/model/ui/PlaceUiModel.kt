package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.ui.utils.Message

data class PlaceUiModel(
    val osmId: String,
    val placeType: PlaceType,
    val primaryText: String,
    val secondaryText: Message?,
    @DrawableRes val iconRes: Int
)

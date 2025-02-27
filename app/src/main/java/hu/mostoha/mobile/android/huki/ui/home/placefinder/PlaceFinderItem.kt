package hu.mostoha.mobile.android.huki.ui.home.placefinder

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel

sealed class PlaceFinderItem {

    data class Place(val placeUiModel: PlaceUiModel) : PlaceFinderItem()

    data object StaticActions : PlaceFinderItem()

    data object ShowMoreHistory : PlaceFinderItem()

    data object Loading : PlaceFinderItem()

    data class Info(
        @DrawableRes val drawableRes: Int,
        val messageRes: Message.Res
    ) : PlaceFinderItem()

    data object Attribution : PlaceFinderItem()

}

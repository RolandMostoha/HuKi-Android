package hu.mostoha.mobile.android.huki.ui.home.searchbar

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel

sealed class SearchBarItem {

    data class Place(val placeUiModel: PlaceUiModel) : SearchBarItem()

    data class Error(
        @DrawableRes val drawableRes: Int,
        val messageRes: Message.Res
    ) : SearchBarItem()

}

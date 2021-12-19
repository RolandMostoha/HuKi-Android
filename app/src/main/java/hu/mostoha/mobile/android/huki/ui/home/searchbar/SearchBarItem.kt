package hu.mostoha.mobile.android.huki.ui.home.searchbar

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.util.Message

sealed class SearchBarItem {

    data class Place(val placeUiModel: PlaceUiModel) : SearchBarItem()

    data class Error(
        @DrawableRes val drawableRes: Int,
        val messageRes: Message.Res
    ) : SearchBarItem()

}

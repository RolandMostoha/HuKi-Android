package hu.mostoha.mobile.android.huki.ui.home.searchbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel

sealed class SearchBarItem {
    data class Place(val placeUiModel: PlaceUiModel) : SearchBarItem()
    data class Info(@StringRes val messageRes: Int, @DrawableRes val drawableRes: Int) : SearchBarItem()
}
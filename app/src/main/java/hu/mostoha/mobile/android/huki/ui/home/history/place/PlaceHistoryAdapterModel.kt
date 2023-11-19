package hu.mostoha.mobile.android.huki.ui.home.history.place

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel

sealed class PlaceHistoryAdapterModel {

    data class Item(val placeUiModel: PlaceUiModel) : PlaceHistoryAdapterModel()

    data class Header(val dateText: Message) : PlaceHistoryAdapterModel()

    data class InfoView(
        @StringRes val message: Int,
        @DrawableRes val iconRes: Int,
    ) : PlaceHistoryAdapterModel()

}

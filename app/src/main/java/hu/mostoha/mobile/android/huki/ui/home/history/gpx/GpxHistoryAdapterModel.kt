package hu.mostoha.mobile.android.huki.ui.home.history.gpx

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.ui.Message

sealed class GpxHistoryAdapterModel {

    data class Item(
        val name: String,
        val gpxType: GpxType,
        val fileUri: Uri,
        val travelTimeText: Message?,
        val distanceText: Message?,
        val inclineText: Message?,
        val declineText: Message?,
        val waypointCountText: Message?,
    ) : GpxHistoryAdapterModel()

    data class Header(val dateText: Message) : GpxHistoryAdapterModel()

    data class InfoView(
        @StringRes val message: Int,
        @DrawableRes val iconRes: Int,
    ) : GpxHistoryAdapterModel()

}

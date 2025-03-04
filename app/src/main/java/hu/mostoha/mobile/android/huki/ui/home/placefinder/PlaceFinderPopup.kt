package hu.mostoha.mobile.android.huki.ui.home.placefinder

import android.content.Context
import android.view.View
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel

class PlaceFinderPopup(
    private val context: Context,
    onPlaceClick: (PlaceUiModel) -> Unit,
    onMyLocationClick: () -> Unit,
    onPickLocationClick: () -> Unit,
    onShowMoreHistoryClick: (() -> Unit)? = null,
) : ListPopupWindow(context) {

    private var placeFinderAdapter: PlaceFinderAdapter

    init {
        height = context.resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
        inputMethodMode = INPUT_METHOD_NEEDED
        verticalOffset = context.resources.getDimensionPixelSize(R.dimen.space_extra_extra_small)
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.background_dialog))

        placeFinderAdapter = PlaceFinderAdapter(
            context = context,
            onMyLocationClick = {
                onMyLocationClick.invoke()
                dismiss()
            },
            onManualLocationClick = {
                onPickLocationClick.invoke()
                dismiss()
            },
            onShowMoreHistoryClick = {
                onShowMoreHistoryClick?.invoke()
                dismiss()
            },
            onAttributionClick = {
                context.openUrl(context.getString(R.string.location_iq_attribution_url))
                dismiss()
            }
        )
        setAdapter(placeFinderAdapter)

        setOnItemClickListener { _, _, position, _ ->
            val placeFinderItem = placeFinderAdapter.getItem(position)
            if (placeFinderItem != null && placeFinderItem is PlaceFinderItem.Place) {
                onPlaceClick.invoke(placeFinderItem.placeUiModel)
            }
            dismiss()
        }
    }

    fun initPlaceFinderItems(anchor: View, placeItems: List<PlaceFinderItem>) {
        anchorView = anchor
        inputMethodMode = INPUT_METHOD_NEEDED

        val hasPlaceItems = placeItems
            .filterIsInstance<PlaceFinderItem.Place>()
            .isNotEmpty()

        when {
            hasPlaceItems -> {
                placeFinderAdapter.submitList(placeItems)
                width = anchor.width
                height = context.resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
                show()
            }
            else -> {
                placeFinderAdapter.submitList(placeItems)
                width = anchor.width
                height = WRAP_CONTENT
                show()
            }
        }
    }

    fun clearPlaceFinderItems() {
        dismiss()
        placeFinderAdapter.submitList(emptyList())
    }

}

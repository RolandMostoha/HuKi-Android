package hu.mostoha.mobile.android.huki.ui.home.layers

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.WindowPopupLayersBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.ui.HikingLayerUiModel

class LayersPopupWindow(val context: Context) : PopupWindow(context) {

    private var binding = WindowPopupLayersBinding.inflate(context.inflater, null, false)

    init {
        contentView = binding.root
        setBackgroundDrawable(
            InsetDrawable(
                ContextCompat.getDrawable(context, R.drawable.background_dialog),
                0,
                0,
                context.resources.getDimensionPixelSize(R.dimen.space_medium),
                context.resources.getDimensionPixelSize(R.dimen.space_medium)
            )
        )
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = context.resources.getDimensionPixelSize(R.dimen.home_layers_popup_height)
        isOutsideTouchable = true
        isFocusable = true
        elevation = context.resources.getDimension(R.dimen.default_elevation)
    }

    fun updateDialog(hikingLayerState: HikingLayerUiModel, onDownloadButtonClick: () -> Unit) {
        val layersAdapter = LayersAdapter(onItemClick = { })
        layersAdapter.submitList(BaseLayer.values().toList())
        binding.popupLayersBaseLayersList.adapter = layersAdapter

        with(binding.popupLayersHikingLayerContainer) {
            when (hikingLayerState) {
                is HikingLayerUiModel.NotDownloaded -> {
                    itemLayersHikingCard.strokeColor = ContextCompat.getColor(root.context, R.color.colorStroke)
                    itemLayersSelectedImage.gone()
                    itemLayersLastUpdatedText.gone()
                    itemLayersHikingDownloadButton.setText(R.string.layers_hiking_not_download_label)
                    itemLayersHikingDownloadButton.isEnabled = true
                    itemLayersHikingDownloadButton.inProgress = false
                }
                is HikingLayerUiModel.Downloading -> {
                    itemLayersHikingCard.strokeColor = ContextCompat.getColor(root.context, R.color.colorStroke)
                    itemLayersHikingDownloadButton.setText(R.string.layers_hiking_downloading_label)
                    itemLayersHikingDownloadButton.isEnabled = false
                    itemLayersHikingDownloadButton.inProgress = true
                }
                is HikingLayerUiModel.Downloaded -> {
                    itemLayersHikingCard.strokeColor = ContextCompat.getColor(root.context, R.color.colorPrimary)
                    itemLayersSelectedImage.visible()
                    itemLayersLastUpdatedText.setTextOrGone(hikingLayerState.lastUpdatedText)
                    itemLayersHikingDownloadButton.setText(R.string.layers_hiking_downloaded_label)
                    itemLayersHikingDownloadButton.isEnabled = true
                    itemLayersHikingDownloadButton.inProgress = false
                }
                else -> {
                    // no-op
                }
            }

            itemLayersHikingDownloadButton.setOnClickListener {
                onDownloadButtonClick.invoke()
            }
        }
    }

    fun show(anchorView: View) {
        showAsDropDown(anchorView, 0, context.resources.getDimensionPixelSize(R.dimen.space_small))
    }

}

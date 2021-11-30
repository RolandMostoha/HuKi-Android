package hu.mostoha.mobile.android.huki.ui.home.layers

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.WindowPopupLayersBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.ui.HikingLayerDetailsUiModel

class LayersPopupWindow(context: Context) : PopupWindow(context) {

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

    fun updateDialog(uiModel: HikingLayerDetailsUiModel, onDownloadButtonClick: () -> Unit) {
        val layersAdapter = LayersAdapter(onItemClick = { })
        layersAdapter.submitList(BaseLayer.values().toList())
        binding.popupLayersBaseLayersList.adapter = layersAdapter

        with(binding.popupLayersHikingLayerContainer) {
            if (uiModel.isHikingLayerFileDownloaded) {
                itemLayersHikingCard.strokeColor = ContextCompat.getColor(root.context, R.color.colorPrimary)
                itemLayersSelectedImage.visible()
                itemLayersLastUpdatedText.setTextOrGone(uiModel.lastUpdatedText)
                itemLayersHikingDownloadButton.setText(R.string.layers_hiking_update_label)
            } else {
                itemLayersHikingCard.strokeColor = ContextCompat.getColor(root.context, R.color.colorStroke)
                itemLayersSelectedImage.gone()
                itemLayersLastUpdatedText.gone()
                itemLayersHikingDownloadButton.setText(R.string.layers_hiking_download_label)
            }

            itemLayersHikingDownloadButton.setOnClickListener {
                onDownloadButtonClick.invoke()
            }
        }
    }

}

package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.databinding.ItemHomeLandscapesChipBinding
import hu.mostoha.mobile.android.turistautak.databinding.LayoutBottomSheetPlaceDetailsBinding
import hu.mostoha.mobile.android.turistautak.databinding.PopupLayersBinding
import hu.mostoha.mobile.android.turistautak.extensions.gone
import hu.mostoha.mobile.android.turistautak.extensions.setTextOrGone
import hu.mostoha.mobile.android.turistautak.extensions.visible
import hu.mostoha.mobile.android.turistautak.model.ui.HikingLayerDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.turistautak.ui.home.layers.BaseLayer
import hu.mostoha.mobile.android.turistautak.ui.home.layers.LayersAdapter

fun PopupLayersBinding.bindUiModel(
    uiModel: HikingLayerDetailsUiModel,
    onDownloadButtonClick: () -> Unit
) {
    val layersAdapter = LayersAdapter(onItemClick = {
        // TODO If multiple base layer selection is ready
    })
    layersAdapter.submitList(BaseLayer.values().toList())
    popupLayersBaseLayersList.adapter = layersAdapter

    with(popupLayersHikingLayerContainer) {
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

fun ItemHomeLandscapesChipBinding.bindUiModel(
    uiModel: PlaceUiModel,
    onChipClick: () -> Unit
) {
    landscapesChip.text = uiModel.primaryText
    landscapesChip.setChipIconResource(uiModel.iconRes)
    landscapesChip.setOnClickListener {
        onChipClick.invoke()
    }
}

fun LayoutBottomSheetPlaceDetailsBinding.bindWayUiModel(
    uiModel: PlaceUiModel,
    onHikingTrailsButtonClick: () -> Unit,
    onCloseButtonClick: () -> Unit,
) {
    placeDetailsPrimaryText.text = uiModel.primaryText
    placeDetailsSecondaryText.setTextOrGone(uiModel.secondaryText)
    placeDetailsImage.setImageResource(uiModel.iconRes)
    placeDetailsDirectionsButton.gone()
    placeDetailsHikingTrailsButton.setOnClickListener {
        onHikingTrailsButtonClick.invoke()
    }
    placeDetailsCloseButton.setOnClickListener {
        onCloseButtonClick.invoke()
    }
}

fun LayoutBottomSheetPlaceDetailsBinding.bindNodeUiModel(
    place: PlaceUiModel,
    onHikingTrailsButtonClick: () -> Unit,
    onCloseButtonClick: () -> Unit,
    onDirectionsButtonClick: () -> Unit,
) {
    placeDetailsPrimaryText.text = place.primaryText
    placeDetailsSecondaryText.setTextOrGone(place.secondaryText)
    placeDetailsImage.setImageResource(place.iconRes)
    placeDetailsDirectionsButton.visible()
    placeDetailsDirectionsButton.setOnClickListener {
        onDirectionsButtonClick.invoke()
    }
    placeDetailsHikingTrailsButton.setOnClickListener {
        onHikingTrailsButtonClick.invoke()
    }
    placeDetailsCloseButton.setOnClickListener {
        onCloseButtonClick.invoke()
    }
}

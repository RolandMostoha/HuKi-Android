package hu.mostoha.mobile.android.huki.ui.home.placedetails

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetPlaceDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class PlaceDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetPlaceDetailsBinding,
    private val analyticsService: AnalyticsService
) : BottomSheetDialog(binding) {

    fun initNodeBottomSheet(
        placeUiModel: PlaceUiModel,
        onShowAllPointsClick: () -> Unit,
        onRoutePlanButtonClick: () -> Unit,
        onHikeRecommenderClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        postMain {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsButtonGroupScrollView.visible()

                placeDetailsPrimaryText.setTextAppearance(R.style.DefaultTextAppearance_SemiBold_Large)
                placeDetailsPrimaryText.maxLines = 2
                placeDetailsPrimaryText.text = placeName

                placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                placeDetailsGoogleNavButton.visible()
                placeDetailsGoogleNavButton.setOnClickListener {
                    analyticsService.googleMapsClicked()
                    context.startGoogleMapsDirectionsIntent(placeUiModel.geoPoint)
                }
                placeDetailsRoutePlanButton.visible()
                placeDetailsRoutePlanButton.setOnClickListener {
                    onRoutePlanButtonClick.invoke()
                }
                placeDetailsHikingRecommenderButton.setOnClickListener {
                    onHikeRecommenderClick.invoke()
                }
                if (placeUiModel.placeType == PlaceType.NODE) {
                    placeDetailsShowAllPointsButton.gone()
                } else {
                    placeDetailsShowAllPointsButton.visible()
                    placeDetailsShowAllPointsButton.setOnClickListener {
                        analyticsService.loadPlaceDetailsClicked(placeName, placeUiModel.placeType)
                        onShowAllPointsClick.invoke()
                    }
                }
                placeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
            }
            show()
        }
    }

    fun initPolyDetailsBottomSheet(placeUiModel: PlaceUiModel, onCloseButtonClick: () -> Unit) {
        postMain {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsButtonGroupScrollView.gone()

                placeDetailsPrimaryText.setTextAppearance(R.style.DefaultTextAppearance_SemiBold_Medium)
                placeDetailsPrimaryText.maxLines = Int.MAX_VALUE
                placeDetailsPrimaryText.text = placeName
                placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)

                placeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
            }
            show()
        }
    }

}

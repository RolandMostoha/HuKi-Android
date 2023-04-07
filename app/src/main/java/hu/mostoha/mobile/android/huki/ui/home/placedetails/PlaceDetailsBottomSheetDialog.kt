package hu.mostoha.mobile.android.huki.ui.home.placedetails

import android.view.View
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetPlaceDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class PlaceDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetPlaceDetailsBinding,
    private val analyticsService: FirebaseAnalyticsService
) : BottomSheetDialog(binding) {

    fun initNodeBottomSheet(
        placeUiModel: PlaceUiModel,
        onShowAllPointsClick: () -> Unit,
        onRoutePlanButtonClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        postMain {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsPrimaryText.text = placeName
                placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                placeDetailsGoogleNavButton.visible()
                placeDetailsGoogleNavButton.setOnClickListener {
                    analyticsService.googleMapsClicked(placeName)

                    context.startGoogleMapsDirectionsIntent(placeUiModel.geoPoint)
                }
                placeDetailsHikingTrailsButton.gone()
                placeDetailsRoutePlanButton.visible()
                placeDetailsRoutePlanButton.setOnClickListener {
                    onRoutePlanButtonClick.invoke()
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

                placeDetailsButtonGroupScrollView.fullScroll(View.FOCUS_RIGHT)
            }
            show()
        }
    }

    fun initWayBottomSheet(
        placeUiModel: PlaceUiModel,
        onHikingTrailsButtonClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        postMain {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsPrimaryText.text = placeName
                placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                placeDetailsGoogleNavButton.gone()
                placeDetailsShowAllPointsButton.gone()
                placeDetailsHikingTrailsButton.visible()
                placeDetailsHikingTrailsButton.setOnClickListener {
                    analyticsService.loadHikingRoutesClicked(placeName)
                    onHikingTrailsButtonClick.invoke()
                }
                placeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
                placeDetailsRoutePlanButton.gone()

                placeDetailsButtonGroupScrollView.fullScroll(View.FOCUS_RIGHT)
            }
            show()
        }
    }

}

package hu.mostoha.mobile.android.huki.ui.home.placedetails

import android.os.Handler
import android.os.Looper
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetPlaceDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.gone
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
        onHikingTrailsButtonClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        Handler(Looper.getMainLooper()).post {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsPrimaryText.text = placeName
                placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                placeDetailsDirectionsButton.visible()
                placeDetailsDirectionsButton.setOnClickListener {
                    analyticsService.navigationClicked(placeName)

                    context.startGoogleMapsDirectionsIntent(placeUiModel.geoPoint)
                }
                if (placeUiModel.placeType != PlaceType.NODE) {
                    placeDetailsHikingTrailsButton.gone()
                    placeDetailsShowAllPointsButton.visible()
                    placeDetailsShowAllPointsButton.setOnClickListener {
                        analyticsService.loadPlaceDetailsClicked(placeName, placeUiModel.placeType)
                        onShowAllPointsClick.invoke()
                    }
                } else {
                    placeDetailsShowAllPointsButton.gone()
                    placeDetailsHikingTrailsButton.visible()
                    placeDetailsHikingTrailsButton.setOnClickListener {
                        val placeTitle = context.getString(
                            R.string.map_place_name_node_routes_nearby,
                            placeUiModel.primaryText.resolve(context)
                        )
                        analyticsService.loadHikingRoutesClicked(placeTitle)
                        onHikingTrailsButtonClick.invoke()
                    }
                }
                placeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
            }
            show()
        }
    }

    fun initWayBottomSheet(
        placeUiModel: PlaceUiModel,
        onHikingTrailsButtonClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        Handler(Looper.getMainLooper()).post {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsPrimaryText.text = placeName
                placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                placeDetailsDirectionsButton.gone()
                placeDetailsShowAllPointsButton.gone()
                placeDetailsHikingTrailsButton.visible()
                placeDetailsHikingTrailsButton.setOnClickListener {
                    analyticsService.loadHikingRoutesClicked(placeName)
                    onHikingTrailsButtonClick.invoke()
                }
                placeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
            }
            show()
        }
    }

}

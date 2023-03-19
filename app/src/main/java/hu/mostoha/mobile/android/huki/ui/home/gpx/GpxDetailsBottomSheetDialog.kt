package hu.mostoha.mobile.android.huki.ui.home.gpx

import android.os.Handler
import android.os.Looper
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetGpxDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class GpxDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetGpxDetailsBinding
) : BottomSheetDialog(binding) {

    fun initBottomSheet(gpxDetails: GpxDetailsUiModel, onCloseClick: () -> Unit, onStartClick: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            with(binding) {
                val hasAltitudeValues = gpxDetails.altitudeUiModel != null

                gpxDetailsPrimaryText.text = gpxDetails.name
                gpxDetailsAltitudeRangeContainer.visibleOrGone(hasAltitudeValues)
                with(binding.gpxDetailsRouteAttributesContainer) {
                    routeAttributesTimeText.setMessage(gpxDetails.travelTimeText)
                    routeAttributesDistanceText.setMessage(gpxDetails.distanceText)

                    routeAttributesUphillTextSeparator.visibleOrGone(hasAltitudeValues)
                    routeAttributesDownhillTextSeparator.visibleOrGone(hasAltitudeValues)
                    routeAttributesUphillText.setMessageOrGone(gpxDetails.altitudeUiModel?.uphillText)
                    routeAttributesDownhillText.setMessageOrGone(gpxDetails.altitudeUiModel?.downhillText)
                }
                gpxDetailsAltitudeRangeStartText.setMessageOrGone(gpxDetails.altitudeUiModel?.minAltitudeText)
                gpxDetailsAltitudeRangeEndText.setMessageOrGone(gpxDetails.altitudeUiModel?.maxAltitudeText)

                gpxDetailsCloseButton.setOnClickListener {
                    onCloseClick.invoke()
                    hide()
                }
                gpxDetailsStartButton.setOnClickListener {
                    onStartClick.invoke()
                    hide()
                }
                gpxDetailsGoogleMapsButton.setOnClickListener {
                    context.startGoogleMapsDirectionsIntent(gpxDetails.start)
                }
            }
            show()
        }
    }

}

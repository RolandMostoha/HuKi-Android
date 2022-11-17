package hu.mostoha.mobile.android.huki.ui.home.gpx

import android.os.Handler
import android.os.Looper
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetGpxDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class GpxDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetGpxDetailsBinding
) : BottomSheetDialog(binding) {

    fun initBottomSheet(gpxDetails: GpxDetailsUiModel, onCloseClick: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            with(binding) {
                val hasAltitudeValues = gpxDetails.gpxAltitudeUiModel != null

                gpxDetailsPrimaryText.text = gpxDetails.name
                gpxDetailsAltitudeRangeContainer.visibleOrGone(hasAltitudeValues)
                gpxDetailsUphillTextSeparator.visibleOrGone(hasAltitudeValues)
                gpxDetailsDownhillTextSeparator.visibleOrGone(hasAltitudeValues)
                if (hasAltitudeValues) {
                    gpxDetailsDistanceText.setMessage(gpxDetails.distanceText)
                } else {
                    gpxDetailsDistanceText.text = context.getString(
                        R.string.gpx_details_bottom_sheet_distance,
                        gpxDetails.distanceText.resolve(context)
                    )
                }
                gpxDetailsUphillText.setMessageOrGone(gpxDetails.gpxAltitudeUiModel?.uphillText)
                gpxDetailsDownhillText.setMessageOrGone(gpxDetails.gpxAltitudeUiModel?.downhillText)
                gpxDetailsAltitudeRangeStartText.setMessageOrGone(gpxDetails.gpxAltitudeUiModel?.minAltitudeText)
                gpxDetailsAltitudeRangeEndText.setMessageOrGone(gpxDetails.gpxAltitudeUiModel?.maxAltitudeText)

                gpxDetailsCloseButton.setOnClickListener {
                    onCloseClick.invoke()
                    hide()
                }
                gpxDetailsNavigateStartButton.setOnClickListener {
                    context.startGoogleMapsDirectionsIntent(gpxDetails.start)
                }
            }
            show()
        }
    }

}

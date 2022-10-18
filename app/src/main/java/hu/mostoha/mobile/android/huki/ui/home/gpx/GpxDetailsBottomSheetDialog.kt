package hu.mostoha.mobile.android.huki.ui.home.gpx

import android.os.Handler
import android.os.Looper
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetGpxDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class GpxDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetGpxDetailsBinding
) : BottomSheetDialog(binding) {

    fun initBottomSheet(gpxDetails: GpxDetailsUiModel, onCloseClick: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            with(binding) {
                gpxDetailsPrimaryText.text = gpxDetails.name
                gpxDetailsSecondaryText.text = context.getString(
                    R.string.gpx_details_bottom_sheet_distance,
                    gpxDetails.distanceText.resolve(context)
                )
                if (gpxDetails.gpxAltitudeUiModel != null) {
                    gpxDetailsUphillText.setMessage(gpxDetails.gpxAltitudeUiModel.uphillText)
                    gpxDetailsDownhillText.setMessage(gpxDetails.gpxAltitudeUiModel.downhillText)
                    gpxDetailsAltitudeRangeText.text = context.getString(
                        R.string.gpx_details_bottom_sheet_altitude_range,
                        gpxDetails.gpxAltitudeUiModel.minAltitudeText.resolve(context),
                        gpxDetails.gpxAltitudeUiModel.maxAltitudeText.resolve(context)
                    )
                } else {
                    gpxDetailsAltitudeDetailsContainer.gone()
                }
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

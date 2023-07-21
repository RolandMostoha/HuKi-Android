package hu.mostoha.mobile.android.huki.ui.home.gpx

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetGpxDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class GpxDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetGpxDetailsBinding
) : BottomSheetDialog(binding) {

    fun initBottomSheet(
        gpxDetails: GpxDetailsUiModel,
        onCloseClick: () -> Unit,
        onStartClick: () -> Unit,
        onHideClick: () -> Unit,
    ) {
        postMain {
            with(binding) {
                val hasAltitudeValues = gpxDetails.altitudeUiModel != null

                gpxDetailsPrimaryText.text = gpxDetails.name

                gpxDetailsAltitudeRangeContainer.visibleOrGone(hasAltitudeValues)
                with(binding.gpxDetailsRouteAttributesContainer) {
                    routeAttributesTimeText.setMessageOrGone(gpxDetails.travelTimeText)
                    routeAttributesDistanceText.setMessageOrGone(gpxDetails.distanceText)
                    routeAttributesTimeTextSeparator.visibleOrGone(gpxDetails.travelTimeText != null)

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
                gpxDetailsVisibilityButton.setOnClickListener {
                    onHideClick.invoke()
                }
                gpxDetailsGoogleMapsButton.setOnClickListener {
                    context.startGoogleMapsDirectionsIntent(gpxDetails.geoPoints.first())
                }

                val hasWaypointsOnly = gpxDetails.geoPoints.isEmpty() && gpxDetails.waypoints.isNotEmpty()

                gpxDetailsWaypointsOnlyText.visibleOrGone(hasWaypointsOnly)
                gpxDetailsActionButtonContainer.visibleOrGone(!hasWaypointsOnly)

                if (hasWaypointsOnly) {
                    gpxDetailsWaypointsOnlyText.setMessage(
                        Message.Res(
                            R.string.gpx_details_bottom_sheet_waypoints_only_counter_template,
                            listOf(gpxDetails.waypoints.size)
                        )
                    )
                }
            }
            show()
        }
    }

}

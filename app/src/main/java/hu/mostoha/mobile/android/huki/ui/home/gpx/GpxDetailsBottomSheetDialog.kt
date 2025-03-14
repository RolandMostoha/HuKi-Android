package hu.mostoha.mobile.android.huki.ui.home.gpx

import android.net.Uri
import android.view.View
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetGpxDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuActionItem
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.shareFile
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class GpxDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetGpxDetailsBinding,
    private val analyticsService: AnalyticsService,
) : BottomSheetDialog(binding) {

    fun initBottomSheet(
        gpxDetails: GpxDetailsUiModel,
        onCloseClick: () -> Unit,
        onStartClick: () -> Unit,
        onHideClick: () -> Unit,
        onCommentsButtonClick: () -> Unit,
    ) {
        with(binding) {
            val hasAltitudeValues = gpxDetails.altitudeUiModel != null
            val hasWaypointsOnly = gpxDetails.geoPoints.isEmpty() && gpxDetails.waypoints.isNotEmpty()
            val hasWaypointsComments = gpxDetails.waypoints.any { it.name != null || it.description != null }

            gpxDetailsPrimaryText.text = gpxDetails.name

            gpxDetailsAltitudeRangeContainer.visibleOrGone(hasAltitudeValues)
            with(gpxDetailsRouteAttributesContainer) {
                routeAttributesTimeText.setMessageOrGone(gpxDetails.travelTimeText)
                routeAttributesDistanceText.setMessageOrGone(gpxDetails.distanceText)
                routeAttributesUphillText.setMessageOrGone(gpxDetails.altitudeUiModel?.uphillText)
                routeAttributesDownhillText.setMessageOrGone(gpxDetails.altitudeUiModel?.downhillText)
            }

            gpxDetailsRouteAttributesContainer.routeAttributesWaypointCountText.visibleOrGone(hasWaypointsOnly)
            gpxDetailsActionButtonContainer.visibleOrGone(!hasWaypointsOnly)

            if (hasWaypointsOnly) {
                analyticsService.gpxDetailsWaypointsOnlyImported()
                gpxDetailsRouteAttributesContainer.routeAttributesWaypointCountText.setMessage(
                    Message.Res(
                        R.string.gpx_details_bottom_sheet_waypoints_only_counter_template,
                        listOf(gpxDetails.waypoints.size)
                    )
                )
            }

            gpxDetailsAltitudeRangeStartText.setMessageOrGone(gpxDetails.altitudeUiModel?.minAltitudeText)
            gpxDetailsAltitudeRangeEndText.setMessageOrGone(gpxDetails.altitudeUiModel?.maxAltitudeText)

            gpxDetailsCloseButton.setOnClickListener {
                onCloseClick.invoke()
                hide()
            }
            gpxDetailsStartButton.setOnClickListener {
                analyticsService.gpxDetailsStartClicked()
                onStartClick.invoke()
                hide()
            }
            gpxDetailsVisibilityButton.setOnClickListener {
                analyticsService.gpxDetailsVisibilityClicked()
                onHideClick.invoke()
            }
            gpxDetailsCommentsButton.visibleOrGone(hasWaypointsComments)
            gpxDetailsCommentsButton.setOnClickListener {
                onCommentsButtonClick.invoke()
            }
            gpxDetailsGoogleMapsButton.setOnClickListener {
                val startWaypoint = gpxDetails.waypoints.find { it.waypointType == WaypointType.START }
                if (startWaypoint != null) {
                    showNavigationPopupMenu(gpxDetailsGoogleMapsButton, gpxDetails)
                } else {
                    analyticsService.googleMapsClicked()
                    context.startGoogleMapsDirectionsIntent(gpxDetails.geoPoints.first())
                }
            }
            gpxDetailsShareButton.setOnClickListener {
                analyticsService.gpxDetailsShareClicked()
                context.shareFile(Uri.parse(gpxDetails.fileUri))
            }
        }
    }

    private fun showNavigationPopupMenu(anchorView: View, gpxDetails: GpxDetailsUiModel) {
        context.showPopupMenu(
            anchorView = anchorView,
            actionItems = listOf(
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.gpx_details_bottom_sheet_google_maps_start,
                        iconId = R.drawable.ic_popup_menu_google_maps_start
                    ),
                    onClick = {
                        analyticsService.googleMapsClicked()
                        context.startGoogleMapsDirectionsIntent(gpxDetails.geoPoints.first())
                    }
                ),
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.gpx_details_bottom_sheet_google_maps_end,
                        iconId = R.drawable.ic_popup_menu_google_maps_end
                    ),
                    onClick = {
                        analyticsService.googleMapsClicked()
                        context.startGoogleMapsDirectionsIntent(gpxDetails.geoPoints.last())
                    }
                ),
            ),
            width = R.dimen.default_popup_menu_width_with_header,
            showAtCenter = true,
            headerTitle = R.string.gpx_details_bottom_sheet_google_maps_header.toMessage(),
        )
    }

}

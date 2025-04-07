package hu.mostoha.mobile.android.huki.ui.home.gpx

import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.widget.TextViewCompat
import androidx.transition.TransitionManager
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemGpxDetailsSlopeRowBinding
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetGpxDetailsBinding
import hu.mostoha.mobile.android.huki.databinding.ViewGpxDetailsSlopePopupBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuActionItem
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setDrawableStart
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.shareFile
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.switchVisibility
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.SLOPE_ROWS
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.util.getSlopeGradientDrawable
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
        onReverseSwitched: (Boolean) -> Unit,
        onSlopeColorSwitched: (Boolean) -> Unit,
    ) {
        with(binding) {
            val hasAltitudeValues = gpxDetails.altitudeUiModel != null
            val hasWaypointsOnly = gpxDetails.geoPoints.isEmpty() && gpxDetails.waypoints.isNotEmpty()
            val hasWaypointsComments = gpxDetails.waypoints.any { it.name != null || it.description != null }
            val useSlopeColors = gpxDetails.useSlopeColors

            gpxDetailsPrimaryText.text = gpxDetails.name

            gpxDetailsSlopeRangeContainer.visibleOrGone(hasAltitudeValues && useSlopeColors && !hasWaypointsOnly)
            gpxDetailsSlopeRangeContainer.setOnClickListener {
                showSlopeExplanationPopup()
            }

            gpxDetailsSettingsSlopeSwitch.visibleOrGone(hasAltitudeValues)
            gpxDetailsSettingsSlopeSwitch.isChecked = useSlopeColors

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

            gpxDetailsSettingsButton.setOnClickListener {
                TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup)
                gpxDetailsSettingsContainer.switchVisibility()
            }

            gpxDetailsSettingsSlopeSwitch.setOnCheckedChangeListener { _, isChecked ->
                TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup)
                gpxDetailsSlopeRangeContainer.switchVisibility()
                onSlopeColorSwitched.invoke(isChecked)
            }
            gpxDetailsSettingsReverseSwitch.setOnCheckedChangeListener { _, isChecked ->
                onReverseSwitched.invoke(isChecked)
            }

            gpxDetailsAltitudeRangeView.background = getSlopeGradientDrawable(context)

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
                context.shareFile(gpxDetails.fileUri.toUri())
            }
        }
    }

    private fun showSlopeExplanationPopup() {
        context.showPopupMenu(
            anchorView = binding.root,
            actionItems = emptyList(),
            width = R.dimen.default_popup_menu_width_with_header,
            showAtCenter = true,
            headerTitle = R.string.gpx_slope_explanation_title.toMessage(),
            footerView = ViewGpxDetailsSlopePopupBinding.inflate(context.inflater, null, false).apply {
                SLOPE_ROWS.forEach {
                    gpxDetailsSlopeTable.addView(
                        ItemGpxDetailsSlopeRowBinding.inflate(context.inflater, this.root, false).apply {
                            TextViewCompat.setCompoundDrawableTintList(
                                gpxDetailsSlopeTitle, it.color.color(context).colorStateList()
                            )
                            gpxDetailsSlopeTitle.setDrawableStart(R.drawable.ic_slope_explanation_color)
                            gpxDetailsSlopeTitle.setMessage(it.title)
                            gpxDetailsSlopeDescription.setMessage(it.description)
                        }.root
                    )
                }
            }.root
        )
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

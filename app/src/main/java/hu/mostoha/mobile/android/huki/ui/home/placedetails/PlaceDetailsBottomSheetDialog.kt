package hu.mostoha.mobile.android.huki.ui.home.placedetails

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetPlaceDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.OSM_ID_UNKNOWN_PREFIX
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class PlaceDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetPlaceDetailsBinding,
    private val analyticsService: AnalyticsService
) : BottomSheetDialog(binding) {

    fun initNodeBottomSheet(
        placeUiModel: PlaceUiModel,
        routePlanMarkerCount: Int,
        onShowAllPointsClick: () -> Unit,
        onRoutePlanButtonClick: () -> Unit,
        onPlaceCategoryFinderClick: () -> Unit,
        onAllOsmDataClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        postMain {
            with(binding) {
                val placeName = placeUiModel.primaryText.resolve(root.context)

                placeDetailsButtonGroupScrollView.visible()

                placeDetailsPrimaryText.setTextAppearance(R.style.DefaultTextAppearance_SemiBold_Large)
                placeDetailsPrimaryText.maxLines = 2
                placeDetailsPrimaryText.text = placeName

                placeDetailsSecondaryText.setMessage(placeUiModel.secondaryText)

                if (placeUiModel.osmId.startsWith(OSM_ID_UNKNOWN_PREFIX)) {
                    placeDetailsOsmDataButton.gone()
                } else {
                    placeDetailsOsmDataButton.visible()
                    placeDetailsOsmDataButton.setOnClickListener {
                        if (placeUiModel.osmTags != null) {
                            context.showPopupMenu(
                                anchorView = binding.root,
                                actionItems = emptyList(),
                                width = R.dimen.default_popup_menu_width_with_header,
                                showAtCenter = true,
                                headerTitle = R.string.osm_data_popup_title.toMessage(),
                                footerMessage = R.string.place_details_osm_id_template
                                    .toMessage(listOf(placeUiModel.osmId))
                                    .resolve(context)
                                    .plus(placeUiModel.osmTags)
                                    .toMessage()
                            )
                        } else {
                            analyticsService.allOsmDataClicked()
                            onAllOsmDataClick.invoke()
                        }
                    }
                }

                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                if (placeUiModel.placeType == PlaceType.HIKING_ROUTE) {
                    placeDetailsImage.setBackgroundResource(R.color.transparent)
                } else {
                    placeDetailsImage.setBackgroundResource(R.drawable.background_badge)
                    placeDetailsImage.imageTintList = R.color.colorPrimaryIcon.colorStateList(root.context)
                }
                placeDetailsGoogleNavButton.visible()
                placeDetailsGoogleNavButton.setOnClickListener {
                    analyticsService.googleMapsClicked()
                    context.startGoogleMapsDirectionsIntent(placeUiModel.geoPoint)
                }
                placeDetailsRoutePlanButton.text = if (routePlanMarkerCount > 1) {
                    context.getString(R.string.home_bottom_sheet_route_plan_button_template, routePlanMarkerCount)
                } else {
                    context.getString(R.string.home_bottom_sheet_route_plan_button)
                }
                placeDetailsRoutePlanButton.visible()
                placeDetailsRoutePlanButton.setOnClickListener {
                    onRoutePlanButtonClick.invoke()
                }
                placeDetailsFinderButton.setOnClickListener {
                    onPlaceCategoryFinderClick.invoke()
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
                placeDetailsSecondaryText.setMessage(placeUiModel.secondaryText)
                placeDetailsImage.setImageResource(placeUiModel.iconRes)
                if (placeUiModel.placeType == PlaceType.HIKING_ROUTE) {
                    placeDetailsImage.setBackgroundResource(R.color.transparent)
                } else {
                    placeDetailsImage.setBackgroundResource(R.drawable.background_badge)
                }

                placeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
            }
            show()
        }
    }

}

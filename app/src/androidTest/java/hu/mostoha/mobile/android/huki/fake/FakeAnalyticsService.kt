package hu.mostoha.mobile.android.huki.fake

import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.ui.BillingAction
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import javax.inject.Inject

class FakeAnalyticsService @Inject constructor() : AnalyticsService {

    override fun placeFinderPlaceClicked(searchText: String, placeName: String, isFromHistory: Boolean) = Unit

    override fun loadLandscapeClicked(placeName: String) = Unit

    override fun placeDetailsHikeRecommenderClicked() = Unit

    override fun hikeRecommenderKirandulastippekClicked(placeName: String) = Unit

    override fun hikeRecommenderTermeszetjaroClicked(placeName: String) = Unit

    override fun hikeRecommenderInfoCloseClicked() = Unit

    override fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType) = Unit

    override fun loadHikingRoutesClicked(placeName: String) = Unit

    override fun loadHikingRouteDetailsClicked(routeName: String) = Unit

    override fun myLocationClicked() = Unit

    override fun routePlannerClicked() = Unit

    override fun routePlannerPickLocationClicked() = Unit

    override fun routePlannerMyLocationClicked() = Unit

    override fun routePlanSaved(routePlan: RoutePlanUiModel) = Unit

    override fun googleMapsClicked() = Unit

    override fun gpxImportClicked() = Unit

    override fun gpxImported(fileName: String) = Unit

    override fun gpxImportedByIntent() = Unit

    override fun gpxImportedByFileExplorer() = Unit

    override fun gpxWaypointClicked() = Unit

    override fun gpxDetailsStartClicked() = Unit

    override fun gpxDetailsVisibilityClicked() = Unit

    override fun gpxDetailsShareClicked() = Unit

    override fun gpxDetailsWaypointsOnlyImported() = Unit

    override fun onLayerSelected(layerType: LayerType) = Unit

    override fun settingsClicked() = Unit

    override fun settingsMapScaleInfoClicked() = Unit

    override fun settingsMapScaleSet(mapScalePercentage: Long) = Unit

    override fun settingsEmailClicked() = Unit

    override fun settingsGitHubClicked() = Unit

    override fun settingsGooglePlayReviewClicked() = Unit

    override fun settingsThemeClicked(theme: Theme) = Unit

    override fun settingsOfflineModeInfoClicked() = Unit

    override fun gpxHistoryClicked() = Unit

    override fun gpxHistoryItemOpened(gpxType: GpxType) = Unit

    override fun gpxHistoryItemShared() = Unit

    override fun gpxHistoryItemDelete() = Unit

    override fun gpxHistoryItemRename() = Unit

    override fun placeHistoryItemOpen() = Unit

    override fun placeHistoryItemDelete() = Unit

    override fun oktChipClicked() = Unit

    override fun oktRouteClicked(oktId: String) = Unit

    override fun oktRouteLinkClicked(oktId: String) = Unit

    override fun oktRouteEdgePointClicked(oktId: String) = Unit

    override fun oktGpxImported(fileName: String) = Unit

    override fun oktWaypointClicked() = Unit

    override fun myLocationPlaceRequested() = Unit

    override fun pickLocationPlaceRequested() = Unit

    override fun oktWaypointPlaceRequested() = Unit

    override fun gpxWaypointPlaceRequested() = Unit

    override fun copyrightClicked() = Unit

    override fun hikeModeClicked() = Unit

    override fun liveCompassClicked() = Unit

    override fun supportClicked() = Unit

    override fun supportEmailClicked() = Unit

    override fun supportRecurringLevel1Clicked() = Unit

    override fun supportRecurringLevel2Clicked() = Unit

    override fun supportOneTimeLevel1Clicked() = Unit

    override fun supportOneTimeLevel2Clicked() = Unit

    override fun billingEvent(billingAction: BillingAction, billingResponseCode: Int) = Unit

}

package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.ui.BillingAction
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel

@Suppress("TooManyFunctions")
interface AnalyticsService {

    fun placeFinderPlaceClicked(searchText: String, placeName: String, isFromHistory: Boolean)

    fun loadLandscapeClicked(placeName: String)

    fun placeDetailsFinderClicked()

    fun hikeRecommenderKirandulastippekClicked()

    fun hikeRecommenderTermeszetjaroClicked()

    fun hikeRecommenderInfoCloseClicked()

    fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType)

    fun loadHikingRoutesClicked()

    fun loadHikingRouteDetailsClicked(routeName: String)

    fun myLocationClicked()

    fun routePlannerClicked()

    fun routePlannerPickLocationClicked()

    fun routePlannerMyLocationClicked()

    fun routePlanSaved(routePlan: RoutePlanUiModel)

    fun googleMapsClicked()

    fun gpxImportClicked()

    fun gpxImported(fileName: String)

    fun gpxImportedByIntent()

    fun gpxImportedByFileExplorer()

    fun gpxWaypointClicked()

    fun gpxDetailsStartClicked()

    fun gpxDetailsVisibilityClicked()

    fun gpxDetailsShareClicked()

    fun gpxDetailsWaypointsOnlyImported()

    fun layersClicked()

    fun onLayerSelected(layerType: LayerType)

    fun settingsClicked()

    fun settingsMapScaleInfoClicked()

    fun settingsMapScaleSet(mapScalePercentage: Long)

    fun settingsEmailClicked()

    fun settingsGitHubClicked()

    fun settingsGooglePlayReviewClicked()

    fun settingsThemeClicked(theme: Theme)

    fun settingsOfflineModeInfoClicked()

    fun historyClicked()

    fun gpxHistoryClicked()

    fun gpxHistoryItemOpened(gpxType: GpxType)

    fun gpxHistoryItemShared()

    fun gpxHistoryItemDelete()

    fun gpxHistoryItemRename()

    fun placeHistoryItemOpen()

    fun placeHistoryItemDelete()

    fun oktClicked(oktType: OktType)

    fun oktRouteClicked(oktId: String)

    fun oktRouteLinkClicked(oktId: String)

    fun oktRouteEdgePointClicked(oktId: String)

    fun oktGpxImported(fileName: String)

    fun oktWaypointClicked()

    fun myLocationPlaceRequested()

    fun pickLocationPlaceRequested()

    fun oktWaypointPlaceRequested()

    fun gpxWaypointPlaceRequested()

    fun copyrightClicked()

    fun hikeModeClicked()

    fun liveCompassClicked()

    fun supportClicked()

    fun supportEmailClicked()

    fun supportRecurringLevel1Clicked()

    fun supportRecurringLevel2Clicked()

    fun supportOneTimeLevel1Clicked()

    fun supportOneTimeLevel2Clicked()

    fun newFeaturesSeen(version: String)

    fun billingEvent(billingAction: BillingAction, billingResponseCode: Int)

    fun placeCategoryFabClicked()

    fun placeCategoryClicked(placeCategory: PlaceCategory)

    fun placeCategoryLoaded(numberOfPlaces: Int)

    fun allOsmDataClicked()

}

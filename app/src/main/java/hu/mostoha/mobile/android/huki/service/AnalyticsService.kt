package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel

interface AnalyticsService {

    fun placeFinderPlaceClicked(searchText: String, placeName: String)

    fun loadLandscapeClicked(placeName: String)

    fun placeDetailsHikeRecommenderClicked()

    fun hikeRecommenderKirandulastippekClicked(placeName: String)

    fun hikeRecommenderTermeszetjaroClicked(placeName: String)

    fun hikeRecommenderInfoCloseClicked()

    fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType)

    fun loadHikingRoutesClicked(placeName: String)

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

    fun gpxDetailsStartClicked()

    fun gpxDetailsVisibilityClicked()

    fun gpxDetailsShareClicked()

    fun gpxDetailsWaypointsOnlyImported()

    fun onLayerSelected(layerType: LayerType)

    fun settingsClicked()

    fun settingsMapScaleInfoClicked()

    fun settingsMapScaleSet(mapScalePercentage: Long)

    fun settingsEmailClicked()

    fun settingsGitHubClicked()

    fun settingsGooglePlayReviewClicked()

    fun settingsThemeClicked(theme: Theme)

    fun settingsOfflineModeInfoClicked()

    fun gpxHistoryClicked()

    fun gpxHistoryItemOpened(gpxType: GpxType)

    fun gpxHistoryItemShared()

    fun gpxHistoryItemDelete()

    fun gpxHistoryItemRename()

    fun oktChipClicked()

    fun oktRouteClicked(oktId: String)

    fun oktRouteLinkClicked(oktId: String)

    fun oktGpxImported(fileName: String)

    fun copyrightClicked()

    fun destroyed()

}

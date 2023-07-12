package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel

class FakeAnalyticsService : AnalyticsService {

    override fun placeFinderPlaceClicked(searchText: String, placeName: String) = Unit

    override fun loadLandscapeClicked(placeName: String) = Unit

    override fun landscapeKirandulastippekClicked(placeName: String) = Unit

    override fun landscapeKirandulastippekInfoClicked() = Unit

    override fun landscapeTermeszetjaroClicked(placeName: String) = Unit

    override fun landscapeTermeszetjaroInfoClicked() = Unit

    override fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType) = Unit

    override fun loadHikingRoutesClicked(placeName: String) = Unit

    override fun loadHikingRouteDetailsClicked(routeName: String) = Unit

    override fun myLocationClicked() = Unit

    override fun routePlannerClicked() = Unit

    override fun routePlannerPickLocationClicked() = Unit

    override fun routePlannerMyLocationClicked() = Unit

    override fun routePlanSaved(routePlan: RoutePlanUiModel) = Unit

    override fun googleMapsClicked(destinationPlaceName: String) = Unit

    override fun gpxImportClicked() = Unit

    override fun gpxImported(fileName: String) = Unit

    override fun gpxImportedByIntent() = Unit

    override fun gpxImportedByFileExplorer() = Unit

    override fun onLayerSelected(layerType: LayerType) = Unit

    override fun settingsClicked() = Unit

    override fun settingsMapScaleInfoClicked() = Unit

    override fun settingsMapScaleSet(mapScalePercentage: Long) = Unit

    override fun settingsEmailClicked() = Unit

    override fun settingsGitHubClicked() = Unit

    override fun settingsGooglePlayReviewClicked() = Unit

    override fun settingsThemeClicked(theme: Theme) = Unit

    override fun gpxHistoryClicked() = Unit

    override fun gpxHistoryItemOpened(gpxType: GpxType) = Unit

    override fun gpxHistoryItemShared() = Unit

    override fun copyrightClicked() = Unit

    override fun destroyed() = Unit

}

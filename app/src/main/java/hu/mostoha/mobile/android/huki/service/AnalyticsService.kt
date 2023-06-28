package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel

interface AnalyticsService {

    fun placeFinderPlaceClicked(searchText: String, placeName: String)

    fun loadLandscapeClicked(placeName: String)

    fun landscapeKirandulastippekClicked(placeName: String)

    fun landscapeKirandulastippekInfoClicked()

    fun landscapeTermeszetjaroClicked(placeName: String)

    fun landscapeTermeszetjaroInfoClicked()

    fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType)

    fun loadHikingRoutesClicked(placeName: String)

    fun loadHikingRouteDetailsClicked(routeName: String)

    fun myLocationClicked()

    fun routePlannerClicked()

    fun routePlannerPickLocationClicked()

    fun routePlannerMyLocationClicked()

    fun routePlanSaved(routePlan: RoutePlanUiModel)

    fun googleMapsClicked(destinationPlaceName: String)

    fun gpxImportClicked()

    fun gpxImported(fileName: String)

    fun gpxImportedByIntent()

    fun gpxImportedByFileExplorer()

    fun onLayerSelected(layerType: LayerType)

    fun settingsClicked()

    fun settingsMapScaleInfoClicked()

    fun settingsMapScaleSet(mapScalePercentage: Long)

    fun settingsEmailClicked()

    fun settingsFacebookGroupClicked()

    fun settingsGitHubClicked()

    fun settingsGooglePlayReviewClicked()

    fun copyrightClicked()

    fun destroyed()

}

package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.PlaceType

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

    fun routePlannerDoneClicked()

    fun googleMapsClicked(destinationPlaceName: String)

    fun gpxImportClicked()

    fun gpxImportedByIntent()

    fun gpxImportedByFileExplorer()

    fun settingsClicked()

    fun settingsMapScaleSet(mapScalePercentage: Long)

    fun settingsEmailClicked()

    fun settingsGitHubClicked()

    fun copyrightClicked()

    fun destroyed()

}

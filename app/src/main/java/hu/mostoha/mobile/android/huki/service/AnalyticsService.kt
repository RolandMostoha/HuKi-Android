package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.PlaceType

interface AnalyticsService {

    fun placeFinderPlaceClicked(searchText: String, placeName: String)

    fun loadLandscapeClicked(placeName: String)

    fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType)

    fun loadHikingRoutesClicked(placeName: String)

    fun loadHikingRouteDetailsClicked(routeName: String)

    fun myLocationClicked()

    fun routePlannerClicked()

    fun googleMapsClicked(destinationPlaceName: String)

    fun gpxImportClicked()

    fun gpxImportedByIntent()

    fun gpxImportedByFileExplorer()

    fun copyrightClicked()

    fun destroyed()

}

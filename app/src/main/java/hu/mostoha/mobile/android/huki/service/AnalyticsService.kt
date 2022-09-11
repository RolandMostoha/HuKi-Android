package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.PlaceType

interface AnalyticsService {

    fun searchBarPlaceClicked(searchText: String, placeName: String)

    fun loadLandscapeClicked(placeName: String)

    fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType)

    fun loadHikingRoutesClicked(placeName: String)

    fun loadHikingRouteDetailsClicked(routeName: String)

    fun myLocationClicked()

    fun navigationClicked(destinationPlaceName: String)

    fun copyrightClicked()

    fun destroyed()

}

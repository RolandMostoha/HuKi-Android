package hu.mostoha.mobile.android.huki.service

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType

class FakeAnalyticsService : AnalyticsService {

    override fun searchBarPlaceClicked(searchText: String, placeName: String) = Unit

    override fun loadLandscapeClicked(placeName: String) = Unit

    override fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType) = Unit

    override fun loadHikingRoutesClicked(placeName: String, boundingBox: BoundingBox) = Unit

    override fun loadHikingRouteDetailsClicked(routeName: String) = Unit

    override fun myLocationClicked() = Unit

    override fun navigationClicked(destinationPlaceName: String) = Unit

    override fun copyrightClicked() = Unit

    override fun destroyed() = Unit

}
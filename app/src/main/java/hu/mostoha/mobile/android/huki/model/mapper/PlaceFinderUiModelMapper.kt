package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderItem
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MAX_HISTORY_ITEM
import javax.inject.Inject

class PlaceFinderUiModelMapper @Inject constructor(
    private val placeMapper: PlaceDomainUiMapper,
) {

    fun mapPlaceFinderItems(places: List<Place>, myLocation: Location? = null): List<PlaceFinderItem> {
        return places.map { PlaceFinderItem.Place(placeMapper.mapHistoryPlace(it, myLocation)) }
    }

    fun mapHistoryItems(
        feature: PlaceFinderFeature,
        places: List<Place>,
        myLocation: Location?
    ): List<PlaceFinderItem> {
        return if (feature == PlaceFinderFeature.MAP && places.size > PLACE_FINDER_MAX_HISTORY_ITEM) {
            emptyList<PlaceFinderItem>()
                .plus(
                    places
                        .take(PLACE_FINDER_MAX_HISTORY_ITEM)
                        .map { PlaceFinderItem.Place(placeMapper.mapHistoryPlace(it, myLocation)) }
                )
                .plus(PlaceFinderItem.ShowMoreHistory)
        } else {
            places
                .take(PLACE_FINDER_MAX_HISTORY_ITEM)
                .map { PlaceFinderItem.Place(placeMapper.mapHistoryPlace(it, myLocation)) }
        }
    }

    fun mapPlacesErrorItem(domainException: DomainException): List<PlaceFinderItem> {
        return listOf(
            PlaceFinderItem.Info(
                messageRes = domainException.messageRes,
                drawableRes = R.drawable.ic_search_bar_error
            )
        )
    }

}

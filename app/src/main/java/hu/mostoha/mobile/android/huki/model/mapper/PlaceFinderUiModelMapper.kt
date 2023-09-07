package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderItem
import hu.mostoha.mobile.android.huki.util.distanceBetween
import javax.inject.Inject

class PlaceFinderUiModelMapper @Inject constructor() {

    fun mapPlaceFinderItems(places: List<Place>, location: Location? = null): List<PlaceFinderItem> {
        return if (places.isEmpty()) {
            listOf(
                PlaceFinderItem.Error(
                    messageRes = R.string.place_finder_empty_message.toMessage(),
                    drawableRes = R.drawable.ic_search_bar_empty_result
                )
            )
        } else {
            val placeList = mapPlaces(places, location).map { placeUiModel -> PlaceFinderItem.Place(placeUiModel) }

            listOf(PlaceFinderItem.StaticActions).plus(placeList)
        }
    }

    fun mapPlacesErrorItem(domainException: DomainException): List<PlaceFinderItem> {
        return listOf(
            PlaceFinderItem.Error(
                messageRes = domainException.messageRes,
                drawableRes = R.drawable.ic_search_bar_error
            )
        )
    }

    private fun mapPlaces(places: List<Place>, location: Location?): List<PlaceUiModel> {
        return places.map { place ->
            PlaceUiModel(
                osmId = place.osmId,
                placeType = place.placeType,
                primaryText = place.name.toMessage(),
                secondaryText = Message.Text(mapAddress(place)),
                iconRes = when (place.placeType) {
                    PlaceType.NODE -> R.drawable.ic_place_type_node
                    PlaceType.WAY -> R.drawable.ic_place_type_way
                    PlaceType.RELATION, PlaceType.HIKING_ROUTE -> R.drawable.ic_place_type_relation
                },
                geoPoint = place.location.toGeoPoint(),
                boundingBox = place.boundingBox,
                distanceText = location?.let { DistanceFormatter.format(place.location.distanceBetween(location)) }
            )
        }
    }

    private fun mapAddress(place: Place): String {
        return listOfNotNull(
            place.postCode,
            place.city ?: place.country,
            place.street
        ).joinToString(" ")
    }

}

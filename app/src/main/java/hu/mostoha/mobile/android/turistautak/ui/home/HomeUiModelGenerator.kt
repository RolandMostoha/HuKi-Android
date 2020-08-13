package hu.mostoha.mobile.android.turistautak.ui.home

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlacePredictionUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.UiPayLoad
import javax.inject.Inject

class HomeUiModelGenerator @Inject constructor() {

    fun generatePlacesResult(predictions: List<PlacePrediction>): List<PlacePredictionUiModel> {
        return predictions.map {
            PlacePredictionUiModel(
                id = it.id,
                placeType = it.placeType,
                primaryText = it.primaryText,
                secondaryText = it.secondaryText,
                iconRes = when (it.placeType) {
                    PlaceType.NODE -> R.drawable.ic_home_search_bar_type_node
                    PlaceType.WAY -> R.drawable.ic_home_search_bar_type_way
                    PlaceType.RELATION -> R.drawable.ic_home_search_bar_type_relation
                }
            )
        }
    }

    fun generatePlaceDetails(place: PlaceDetails): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            id = place.id,
            payLoad = when (place.payLoad) {
                is PayLoad.Node -> UiPayLoad.Node(place.payLoad.location.toGeoPoint())
                is PayLoad.Way -> UiPayLoad.Way(place.payLoad.locations.map { it.toGeoPoint() })
                is PayLoad.Relation -> UiPayLoad.Relation(place.payLoad.locations.map { it.toGeoPoint() })
            }
        )
    }

}


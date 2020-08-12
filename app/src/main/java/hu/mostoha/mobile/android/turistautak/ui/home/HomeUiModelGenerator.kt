package hu.mostoha.mobile.android.turistautak.ui.home

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceDetails
import hu.mostoha.mobile.android.turistautak.model.domain.PlacePrediction
import hu.mostoha.mobile.android.turistautak.model.domain.toGeoPoint
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlacePredictionUiModel
import javax.inject.Inject

class HomeUiModelGenerator @Inject constructor() {

    fun generatePlacesResult(predictions: List<PlacePrediction>): List<PlacePredictionUiModel> {
        return predictions.map {
            PlacePredictionUiModel(
                id = it.id,
                placeType = it.placeType,
                primaryText = it.primaryText,
                secondaryText = it.secondaryText,
                iconRes = R.drawable.ic_home_search_bar_type_node
            )
        }
    }

    fun generatePlaceDetails(place: PlaceDetails): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            placeId = place.id,
            geoPoint = place.location.toGeoPoint()
        )
    }

}


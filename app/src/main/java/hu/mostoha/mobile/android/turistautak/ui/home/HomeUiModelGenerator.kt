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
                it.id,
                it.primaryText,
                it.secondaryText,
                R.drawable.ic_home_search_bar_poi
            )
        }
    }

    fun generatePlaceDetails(place: PlaceDetails): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            place.id,
            place.coordinates.toGeoPoint()
        )
    }

}


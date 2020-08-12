package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import hu.mostoha.mobile.android.turistautak.R
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class HomeUiModelGenerator @Inject constructor() {

    fun generatePlacesResult(predictions: List<AutocompletePrediction>): List<PlacesResultUiModel> {
        return predictions.map {
            PlacesResultUiModel(
                it.placeId,
                it.getPrimaryText(null).toString(),
                it.getSecondaryText(null).toString(),
                R.drawable.ic_home_search_bar_poi
            )
        }
    }

    fun generatePlaceDetails(place: Place): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            place.id!!,
            GeoPoint(place.latLng!!.latitude, place.latLng!!.longitude)
        )
    }

}

data class PlaceDetailsUiModel(val placeId: String, val geoPoint: GeoPoint)

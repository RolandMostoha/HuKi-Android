package hu.mostoha.mobile.android.turistautak.repository

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.BuildConfig
import hu.mostoha.mobile.android.turistautak.model.domain.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GooglePlacesRepository @Inject constructor(
    @ApplicationContext context: Context
) : PlacesRepository {

    init {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.GOOGLE_CLOUD_API_KEY)
        }
    }

    private var placesClient = Places.createClient(context)

    override suspend fun getPlacesBy(searchText: String): List<PlacePrediction> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountries("HU")
            .setSessionToken(AutocompleteSessionToken.newInstance())
            .setQuery(searchText)
            .build()

        val task = placesClient.findAutocompletePredictions(request)
        return task.await().autocompletePredictions.map {
            PlacePrediction(
                it.placeId,
                PlaceType.NODE,
                it.getPrimaryText(null).toString(),
                it.getSecondaryText(null).toString()
            )
        }
    }

    override suspend fun getPlaceDetails(id: String, placeType: PlaceType): PlaceDetails {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS
        )
        val request = FetchPlaceRequest.newInstance(id, placeFields)

        val task = placesClient.fetchPlace(request)
        val place = task.await().place
        return PlaceDetails(place.id!!, PayLoad.Node(place.latLng!!.toLocation()))
    }

}
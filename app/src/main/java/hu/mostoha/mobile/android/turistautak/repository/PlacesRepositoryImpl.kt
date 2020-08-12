package hu.mostoha.mobile.android.turistautak.repository

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.BuildConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : PlacesRepository {

    init {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.GOOGLE_CLOUD_API_KEY)
        }
    }

    private var placesClient = Places.createClient(context)

    override suspend fun getPlacesBy(searchText: String): List<AutocompletePrediction> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountries("HU")
            .setSessionToken(AutocompleteSessionToken.newInstance())
            .setQuery(searchText)
            .build()

        val task = placesClient.findAutocompletePredictions(request)
        return task.await().autocompletePredictions
    }

    override suspend fun getPlaceDetails(placeId: String): Place {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS
        )
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        val task = placesClient.fetchPlace(request)
        return task.await().place
    }

}
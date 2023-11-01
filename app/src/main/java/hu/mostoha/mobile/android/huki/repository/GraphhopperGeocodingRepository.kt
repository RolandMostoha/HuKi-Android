package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.network.GraphhopperService
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import timber.log.Timber
import javax.inject.Inject

class GraphhopperGeocodingRepository @Inject constructor(
    private val graphhopperService: GraphhopperService,
    private val exceptionLogger: ExceptionLogger,
) : GeocodingRepository {

    override suspend fun getPlacesBy(searchText: String, location: Location?): List<Place> {
        return emptyList()
    }

    override suspend fun getPlace(location: Location): Place? {
        val response = try {
            graphhopperService.reverseGeocode(
                point = LocationFormatter.formatString(location)
            )
        } catch (exception: Exception) {
            Timber.e(exception, "Network error during geocoding address request")

            exceptionLogger.recordException(exception)

            return null
        }

        return response.hits.firstOrNull()?.let {
            Place(
                osmId = it.osmId,
                placeType = PlaceType.NODE,
                name = it.name,
                city = it.city,
                country = it.country,
                postCode = it.postcode,
                street = listOfNotNull(it.street, it.houseNumber).joinToString(" "),
                location = location,
            )
        }
    }

}

package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.mapper.GeocodingDomainModelMapper
import hu.mostoha.mobile.android.huki.network.PhotonService
import timber.log.Timber
import javax.inject.Inject

class PhotonGeocodingRepository @Inject constructor(
    private val photonService: PhotonService,
    private val mapper: GeocodingDomainModelMapper,
    private val exceptionLogger: ExceptionLogger,
) : GeocodingRepository {

    override suspend fun getPlacesBy(searchText: String, location: Location?): List<Place> {
        val response = if (location == null) {
            photonService.query(searchText, GEOCODING_SEARCH_QUERY_LIMIT)
        } else {
            photonService.query(searchText, GEOCODING_SEARCH_QUERY_LIMIT, location.latitude, location.longitude)
        }

        return mapper.mapPlace(response)
    }

    override suspend fun getPlace(location: Location): Place? {
        val response = try {
            photonService.reverseGeocode(
                limit = 1,
                latitude = location.latitude,
                longitude = location.longitude,
            )
        } catch (exception: Exception) {
            Timber.e(exception, "Network error during geocoding address request")

            exceptionLogger.recordException(exception)

            return null
        }

        return mapper.mapPlace(response).firstOrNull()
    }

}

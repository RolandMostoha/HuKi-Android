package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.mapper.PlaceNetworkDomainMapper
import hu.mostoha.mobile.android.huki.network.PhotonService
import timber.log.Timber
import javax.inject.Inject

class PhotonGeocodingRepository @Inject constructor(
    private val photonService: PhotonService,
    private val placeMapper: PlaceNetworkDomainMapper,
    private val exceptionLogger: ExceptionLogger,
) : GeocodingRepository {

    override suspend fun getPlacesBy(searchText: String, placeFeature: PlaceFeature, location: Location?): List<Place> {
        val response = if (location == null) {
            photonService.query(searchText, GEOCODING_SEARCH_QUERY_LIMIT)
        } else {
            photonService.query(searchText, GEOCODING_SEARCH_QUERY_LIMIT, location.latitude, location.longitude)
        }

        return placeMapper.mapPlace(response, placeFeature)
    }

    override suspend fun getPlace(location: Location, placeFeature: PlaceFeature): Place? {
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

        return placeMapper.mapPlace(response, placeFeature).firstOrNull()
    }

    override suspend fun getPlaceProfile(location: Location): PlaceProfile? {
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

        return placeMapper.mapPlaceProfile(response)
    }

}

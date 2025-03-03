package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.toViewBox
import hu.mostoha.mobile.android.huki.model.mapper.LocationIqPlaceNetworkMapper
import hu.mostoha.mobile.android.huki.network.LocationIqService
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

class LocationIqGeocodingRepository @Inject constructor(
    private val locationIqService: LocationIqService,
    private val photonGeocodingRepository: PhotonGeocodingRepository,
    private val placeMapper: LocationIqPlaceNetworkMapper,
    private val exceptionLogger: ExceptionLogger,
) : GeocodingRepository {

    companion object {
        private const val HTTP_RATE_LIMIT_EXCEEDED = 429
    }

    override suspend fun getAutocompletePlaces(searchText: String, boundingBox: BoundingBox): List<PlaceProfile> {
        val response = try {
            locationIqService.autocomplete(searchText, boundingBox.toViewBox())
        } catch (httpException: HttpException) {
            Timber.e(httpException, "LocationIQ: HttpException during geocoding")

            when (httpException.code()) {
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    return emptyList()
                }
                HTTP_RATE_LIMIT_EXCEEDED -> {
                    return photonGeocodingRepository.getAutocompletePlaces(searchText, boundingBox)
                }
                else -> {
                    throw httpException
                }
            }
        }

        return placeMapper.mapPlaces(response)
    }

    override suspend fun getPlaceProfile(location: Location): PlaceProfile? {
        val response = try {
            locationIqService.reverseGeocode(location.latitude, location.longitude)
        } catch (httpException: HttpException) {
            Timber.e(httpException, "LocationIQ: Network error during reverse geocoding")

            when (httpException.code()) {
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    return null
                }
                HTTP_RATE_LIMIT_EXCEEDED -> {
                    return photonGeocodingRepository.getPlaceProfile(location)
                }
                else -> {
                    recordException(httpException)
                    return null
                }
            }
        } catch (exception: Exception) {
            recordException(exception)
            return null
        }

        return placeMapper.mapPlaceProfile(response)
    }

    private fun recordException(exception: Exception) {
        Timber.e(exception, "LocationIQ: Network error during reverse geocoding")

        exceptionLogger.recordException(exception)
    }

}

package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.toViewBox
import hu.mostoha.mobile.android.huki.model.mapper.PlaceNetworkMapper
import hu.mostoha.mobile.android.huki.network.LocationIqService
import retrofit2.HttpException
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

class LocationIqGeocodingRepository @Inject constructor(
    private val locationIqService: LocationIqService,
    private val placeMapper: PlaceNetworkMapper,
    private val exceptionLogger: ExceptionLogger,
) : GeocodingRepository {

    override suspend fun getAutocompletePlaces(searchText: String, boundingBox: BoundingBox): List<PlaceProfile> {
        val response = try {
            locationIqService.autocomplete(searchText, boundingBox.toViewBox())
        } catch (httpException: HttpException) {
            if (httpException.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                return emptyList()
            } else {
                throw httpException
            }
        }

        return placeMapper.mapPlaces(response)
    }

    override suspend fun getPlaceProfile(location: Location): PlaceProfile? {
        val response = try {
            locationIqService.reverseGeocode(location.latitude, location.longitude)
        } catch (exception: Exception) {
            Timber.e(exception, "LocationIQ: Network error during geocoding")

            exceptionLogger.recordException(exception)

            return null
        }

        return placeMapper.mapPlaceProfile(response)
    }

}

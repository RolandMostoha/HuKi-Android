package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.center
import hu.mostoha.mobile.android.huki.model.mapper.PhotonPlaceNetworkMapper
import hu.mostoha.mobile.android.huki.network.PhotonService
import timber.log.Timber
import javax.inject.Inject

class PhotonGeocodingRepository @Inject constructor(
    private val photonService: PhotonService,
    private val placeMapper: PhotonPlaceNetworkMapper,
    private val exceptionLogger: ExceptionLogger,
) : GeocodingRepository {

    override suspend fun getAutocompletePlaces(searchText: String, boundingBox: BoundingBox): List<PlaceProfile> {
        val center = boundingBox.center()
        val response = photonService.query(searchText, center.latitude, center.longitude)

        return placeMapper.mapPlaceProfile(response)
    }

    override suspend fun getPlaceProfile(location: Location): PlaceProfile? {
        val response = try {
            photonService.reverseGeocode(
                latitude = location.latitude,
                longitude = location.longitude,
            )
        } catch (exception: Exception) {
            Timber.e(exception, "Photon: Network error during reverse geocoding")

            exceptionLogger.recordException(exception)

            return null
        }

        return placeMapper.mapPlaceProfile(response).firstOrNull()
    }

}

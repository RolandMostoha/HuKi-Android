package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.util.calculateDecline
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.util.calculateIncline
import hu.mostoha.mobile.android.huki.util.distanceBetween
import io.ticofab.androidgpxparser.parser.domain.Gpx
import javax.inject.Inject

class LayersDomainModelMapper @Inject constructor() {

    companion object {
        private const val GPX_GEO_POINTS_LIMIT = 2
        private const val GPX_CLOSED_DISTANCE_THRESHOLD = 20
    }

    fun mapGpxDetails(fileName: String, gpx: Gpx): GpxDetails {
        val locations = gpx.tracks
            .flatMap { it.trackSegments }
            .flatMap { it.trackPoints }
            .map { trackPoint ->
                Location(trackPoint.latitude, trackPoint.longitude, trackPoint.elevation)
            }

        if (locations.isEmpty() || locations.size < GPX_GEO_POINTS_LIMIT) {
            throw GpxParseFailedException(IllegalArgumentException("Track is empty"))
        }

        val minAltitude = locations
            .mapNotNull { it.altitude }
            .min()
            .toInt()

        val maxAltitude = locations
            .mapNotNull { it.altitude }
            .max()
            .toInt()

        return GpxDetails(
            fileName = fileName,
            locations = locations,
            distance = locations.calculateDistance(),
            altitudeRange = minAltitude to maxAltitude,
            incline = locations.calculateIncline(),
            decline = locations.calculateDecline(),
            isClosed = locations.first().distanceBetween(locations.last()) <= GPX_CLOSED_DISTANCE_THRESHOLD
        )
    }

}

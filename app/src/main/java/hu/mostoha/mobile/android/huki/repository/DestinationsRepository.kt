package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.Destination
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toOsm
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import javax.inject.Inject

class DestinationsRepository @Inject constructor(
    private val landscapeRepository: LandscapeRepository
) {

    fun getDestinations(placeArea: PlaceArea): List<Destination> {
        return when (placeArea.placeAreaType) {
            is PlaceAreaType.Landscape -> {
                landscapeRepository
                    .getLandscapes()
                    .first { it.osmId == placeArea.placeAreaType.osmId }
                    .destinations

            }
            is PlaceAreaType.PlaceDetails, PlaceAreaType.MapSearch -> {
                landscapeRepository
                    .getLandscapes()
                    .flatMap { it.destinations }
                    .filter { placeArea.boundingBox.toOsm().contains(it.location.toGeoPoint()) }
            }
        }
    }

}

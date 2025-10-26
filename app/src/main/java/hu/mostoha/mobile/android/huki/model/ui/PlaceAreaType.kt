package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.Destination

sealed class PlaceAreaType {

    data object MapSearch : PlaceAreaType()

    data object PlaceDetails : PlaceAreaType()

    data class Landscape(
        val osmId: String,
        val destinations: List<Destination>
    ) : PlaceAreaType()

}

package hu.mostoha.mobile.android.huki.model.ui

sealed class PlaceAreaType {

    /**
     * Place category search initiated from the map mini-fab.
     */
    data object MapSearch : PlaceAreaType()

    /**
     * Place details initiated from marker placement.
     */
    data object PlaceDetails : PlaceAreaType()

    /**
     * Landscapes from Discover-Landscapes feature.
     */
    data class Landscape(val osmId: String) : PlaceAreaType()

}

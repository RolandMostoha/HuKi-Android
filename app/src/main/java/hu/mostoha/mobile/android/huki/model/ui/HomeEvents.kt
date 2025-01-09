package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory

sealed class HomeEvents {

    data class OsmTagsLoaded(val osmId: String, val osmTags: String) : HomeEvents()

    data class PlaceCategoryEmpty(val emptyCategories: Set<PlaceCategory>) : HomeEvents()

}

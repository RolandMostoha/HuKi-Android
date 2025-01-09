package hu.mostoha.mobile.android.huki.model.ui

data class PlaceCategoryUiModel(
    val isAreaLoading: Boolean = true,
    val placeArea: PlaceArea? = null,
    val landscapes: List<LandscapeUiModel>? = null,
)

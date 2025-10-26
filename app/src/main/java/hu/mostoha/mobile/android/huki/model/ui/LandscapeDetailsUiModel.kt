package hu.mostoha.mobile.android.huki.model.ui

data class LandscapeDetailsUiModel(
    val landscapeUiModel: LandscapeUiModel,
    val geometryUiModel: GeometryUiModel.Relation,
    val isSelected: Boolean,
)

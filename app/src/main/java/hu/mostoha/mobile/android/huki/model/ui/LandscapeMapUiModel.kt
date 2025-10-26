package hu.mostoha.mobile.android.huki.model.ui

data class LandscapeMapUiModel(
    val landscapes: List<LandscapeDetailsUiModel>,
    val selectedLandscape: LandscapeDetailsUiModel?,
    val lastSelectedLandscape: LandscapeDetailsUiModel?,
)

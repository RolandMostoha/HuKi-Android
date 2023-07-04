package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.ui.home.gpx.history.GpxHistoryAdapterModel

data class GpxHistoryUiModel(
    val routePlannerGpxList: List<GpxHistoryAdapterModel>,
    val externalGpxList: List<GpxHistoryAdapterModel>,
)

package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.ui.home.history.gpx.GpxHistoryAdapterModel

data class GpxHistoryUiModel(
    val routePlannerGpxList: List<GpxHistoryAdapterModel>,
    val externalGpxList: List<GpxHistoryAdapterModel>,
)

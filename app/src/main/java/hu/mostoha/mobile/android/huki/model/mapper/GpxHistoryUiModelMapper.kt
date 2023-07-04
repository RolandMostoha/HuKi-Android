package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.toLongFormat
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.ui.GpxHistoryUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.ui.home.gpx.history.GpxHistoryAdapterModel
import javax.inject.Inject

class GpxHistoryUiModelMapper @Inject constructor() {

    fun mapToUiModel(gpxHistory: GpxHistory): GpxHistoryUiModel {
        return GpxHistoryUiModel(
            if (gpxHistory.routePlannerGpxList.isEmpty()) {
                listOf(
                    GpxHistoryAdapterModel.InfoView(
                        message = R.string.gpx_history_item_route_planner_empty,
                        iconRes = R.drawable.ic_gpx_history_empty,
                    )
                )
            } else {
                gpxHistory.routePlannerGpxList
                    .sortedByDescending { it.lastModified }
                    .map { gpxHistoryItem ->
                        GpxHistoryAdapterModel.Item(
                            name = gpxHistoryItem.name,
                            gpxType = GpxType.ROUTE_PLANNER,
                            fileUri = gpxHistoryItem.fileUri,
                            dateText = Message.Res(
                                R.string.gpx_history_item_route_planner_date_template,
                                listOf(gpxHistoryItem.lastModified.toLongFormat())
                            )
                        )
                    }
            },
            if (gpxHistory.externalGpxList.isEmpty()) {
                listOf(
                    GpxHistoryAdapterModel.InfoView(
                        message = R.string.gpx_history_item_external_empty,
                        iconRes = R.drawable.ic_gpx_history_empty,
                    )
                )
            } else {
                gpxHistory.externalGpxList
                    .sortedByDescending { it.lastModified }
                    .map { gpxHistoryItem ->
                        GpxHistoryAdapterModel.Item(
                            name = gpxHistoryItem.name,
                            gpxType = GpxType.EXTERNAL,
                            fileUri = gpxHistoryItem.fileUri,
                            dateText = Message.Res(
                                R.string.gpx_history_item_external_date_template,
                                listOf(gpxHistoryItem.lastModified.toLongFormat())
                            )
                        )
                    }
            },
        )
    }

}

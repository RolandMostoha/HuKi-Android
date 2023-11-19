package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatFriendlyDate
import hu.mostoha.mobile.android.huki.extensions.formatLongDateTime
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.ui.GpxHistoryUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.ui.home.history.gpx.GpxHistoryAdapterModel
import hu.mostoha.mobile.android.huki.ui.home.history.place.PlaceHistoryAdapterModel
import java.time.LocalDate
import javax.inject.Inject

class HistoryUiModelMapper @Inject constructor(private val placeMapper: PlaceDomainUiMapper) {

    fun mapGpxHistory(gpxHistory: GpxHistory): GpxHistoryUiModel {
        return GpxHistoryUiModel(
            routePlannerGpxList = if (gpxHistory.routePlannerGpxList.isEmpty()) {
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
                                listOf(gpxHistoryItem.lastModified.formatLongDateTime())
                            )
                        )
                    }
            },
            externalGpxList = if (gpxHistory.externalGpxList.isEmpty()) {
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
                                listOf(gpxHistoryItem.lastModified.formatLongDateTime())
                            )
                        )
                    }
            },
        )
    }

    fun mapPlaceHistory(placeHistory: List<Place>, actualDate: LocalDate): List<PlaceHistoryAdapterModel> {
        return if (placeHistory.isEmpty()) {
            listOf(
                PlaceHistoryAdapterModel.InfoView(
                    message = R.string.place_history_item_empty,
                    iconRes = R.drawable.ic_gpx_history_empty,
                )
            )
        } else {
            placeHistory
                .groupBy { it.historyInfo!!.storeDateTime.toLocalDate() }
                .flatMap { (date, places) ->
                    val headerItem = PlaceHistoryAdapterModel.Header(date.formatFriendlyDate(actualDate))
                    val placeItems = places.map { PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(it)) }

                    listOf(headerItem) + placeItems
                }
        }
    }

}

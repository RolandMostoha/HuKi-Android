package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatFriendlyDate
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.ui.GpxHistoryUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.history.gpx.GpxHistoryAdapterModel
import hu.mostoha.mobile.android.huki.ui.home.history.place.PlaceHistoryAdapterModel
import java.time.LocalDate
import javax.inject.Inject

class HistoryUiModelMapper @Inject constructor(private val placeMapper: PlaceDomainUiMapper) {

    fun mapGpxHistory(gpxHistory: GpxHistory, actualDate: LocalDate): GpxHistoryUiModel {
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
                    .groupBy { it.lastModified.toLocalDate() }
                    .flatMap {
                        val headerItem = GpxHistoryAdapterModel.Header(it.key.formatFriendlyDate(actualDate))
                        val gpxItems = it.value.map { gpxHistoryItem ->
                            gpxHistoryItem.toAdapterModel(GpxType.ROUTE_PLANNER)
                        }

                        listOf(headerItem) + gpxItems
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
                    .groupBy { it.lastModified.toLocalDate() }
                    .flatMap {
                        val headerItem = GpxHistoryAdapterModel.Header(it.key.formatFriendlyDate(actualDate))
                        val gpxItems = it.value.map { gpxHistoryItem ->
                            gpxHistoryItem.toAdapterModel(GpxType.EXTERNAL)
                        }

                        listOf(headerItem) + gpxItems
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
                    val placeItems = places.map { PlaceHistoryAdapterModel.Item(placeMapper.mapToPlaceUiModel(it)) }

                    listOf(headerItem) + placeItems
                }
        }
    }

    private fun GpxHistoryItem.toAdapterModel(gpxType: GpxType): GpxHistoryAdapterModel.Item {
        val gpxHistoryItem = this

        return GpxHistoryAdapterModel.Item(
            name = gpxHistoryItem.name,
            gpxType = gpxType,
            fileUri = gpxHistoryItem.fileUri,
            travelTimeText = if (gpxHistoryItem.travelTime.inWholeSeconds > 0) {
                gpxHistoryItem.travelTime.formatHoursAndMinutes().toMessage()
            } else {
                null
            },
            distanceText = if (gpxHistoryItem.distance > 0) {
                DistanceFormatter.format(gpxHistoryItem.distance)
            } else {
                null
            },
            inclineText = if (gpxHistoryItem.incline > 0) {
                DistanceFormatter.format(gpxHistoryItem.incline)
            } else {
                null
            },
            declineText = if (gpxHistoryItem.decline > 0) {
                DistanceFormatter.format(gpxHistoryItem.decline)
            } else {
                null
            },
            waypointCountText = if (gpxHistoryItem.distance <= 0) {
                Message.Res(
                    R.string.gpx_details_bottom_sheet_waypoints_only_counter_template,
                    listOf(waypointCount)
                )
            } else {
                null
            },
        )
    }

}

package hu.mostoha.mobile.android.huki.model.mapper

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatFriendlyDate
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.HistoryInfo
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.GpxHistoryUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.history.gpx.GpxHistoryAdapterModel
import hu.mostoha.mobile.android.huki.ui.home.history.place.PlaceHistoryAdapterModel
import hu.mostoha.mobile.android.huki.util.DEFAULT_LOCAL_DATE
import io.mockk.mockk
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class HistoryUiModelMapperTest {

    private val hikingRouteRelationMapper = HikingRouteRelationMapper()
    private val placeMapper = PlaceDomainUiMapper(hikingRouteRelationMapper)
    private val mapper = HistoryUiModelMapper(placeMapper)

    @Test
    fun `Given places for today and yesterday empty places, when map place history, then date sectioned adapter list returns`() {
        val places = listOf(
            DEFAULT_PLACE.copy(
                historyInfo = HistoryInfo(
                    isFavourite = false,
                    storeDateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0)
                )
            ),
            DEFAULT_PLACE.copy(
                historyInfo = HistoryInfo(
                    isFavourite = false,
                    storeDateTime = LocalDateTime.of(2022, 12, 31, 0, 0, 0)
                )
            ),
            DEFAULT_PLACE.copy(
                historyInfo = HistoryInfo(
                    isFavourite = false,
                    storeDateTime = LocalDateTime.of(2022, 12, 30, 0, 0, 0)
                )
            ),
            DEFAULT_PLACE.copy(
                historyInfo = HistoryInfo(
                    isFavourite = false,
                    storeDateTime = LocalDateTime.of(2022, 12, 29, 0, 0, 0)
                )
            ),
            DEFAULT_PLACE.copy(
                historyInfo = HistoryInfo(
                    isFavourite = false,
                    storeDateTime = LocalDateTime.of(2022, 12, 20, 0, 0, 0)
                )
            ),
        )
        val actualDate = LocalDate.of(2023, 1, 1)

        val adapterModels = mapper.mapPlaceHistory(places, actualDate)

        assertThat(adapterModels).isEqualTo(
            listOf<PlaceHistoryAdapterModel>()
                .plus(
                    PlaceHistoryAdapterModel.Header(
                        dateText = R.string.default_date_today.toMessage(),
                    )
                )
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places[0])))
                .plus(
                    PlaceHistoryAdapterModel.Header(
                        dateText = R.string.default_date_yesterday.toMessage(),
                    )
                )
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places[1])))
                .plus(
                    PlaceHistoryAdapterModel.Header(
                        dateText = R.string.default_date_friday.toMessage(),
                    )
                )
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places[2])))
                .plus(
                    PlaceHistoryAdapterModel.Header(
                        dateText = R.string.default_date_thursday.toMessage(),
                    )
                )
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places[3])))
                .plus(
                    PlaceHistoryAdapterModel.Header(
                        dateText = "2022.12.20".toMessage(),
                    )
                )
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places[4])))
        )
    }

    @Test
    fun `Given gpx history, when map, then gpx history ui model returns`() {
        val uiModel = mapper.mapGpxHistory(DEFAULT_GPX_HISTORY, DEFAULT_LOCAL_DATE)

        assertThat(uiModel).isEqualTo(
            GpxHistoryUiModel(
                routePlannerGpxList = listOf(
                    GpxHistoryAdapterModel.Header(
                        LocalDate.of(2023, 6, 2).formatFriendlyDate(DEFAULT_LOCAL_DATE)
                    ),
                    GpxHistoryAdapterModel.Item(
                        name = "route_plan_HuKi938.gpx",
                        gpxType = GpxType.ROUTE_PLANNER,
                        fileUri = DEFAULT_ROUTE_PLANNER_GPX_FILE_URI,
                        travelTimeText = 5.hours.formatHoursAndMinutes().toMessage(),
                        distanceText = DistanceFormatter.format(10000),
                        inclineText = DistanceFormatter.format(1000),
                        declineText = DistanceFormatter.format(1000),
                        waypointCountText = null,
                    ),
                ),
                externalGpxList = listOf(
                    GpxHistoryAdapterModel.Header(
                        LocalDate.of(2023, 6, 3).formatFriendlyDate(DEFAULT_LOCAL_DATE)
                    ),
                    GpxHistoryAdapterModel.Item(
                        name = "dera_szurdok.gpx",
                        gpxType = GpxType.EXTERNAL,
                        fileUri = DEFAULT_EXTERNAL_GPX_FILE_URI,
                        travelTimeText = null,
                        distanceText = null,
                        inclineText = null,
                        declineText = null,
                        waypointCountText = Message.Res(
                            R.string.gpx_details_bottom_sheet_waypoints_only_counter_template,
                            listOf(100)
                        ),
                    )
                ),
            )
        )
    }

    companion object {
        private val DEFAULT_PLACE = Place(
            osmId = DEFAULT_NODE_OSM_ID,
            name = DEFAULT_NODE_NAME,
            placeType = PlaceType.NODE,
            address = DEFAULT_NODE_CITY,
            location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.MAP_SEARCH,
        )
        private val DEFAULT_ROUTE_PLANNER_GPX_FILE_URI = mockk<Uri>()
        private val DEFAULT_EXTERNAL_GPX_FILE_URI = mockk<Uri>()
        private val DEFAULT_GPX_HISTORY = GpxHistory(
            routePlannerGpxList = listOf(
                GpxHistoryItem(
                    name = "route_plan_HuKi938.gpx",
                    fileUri = DEFAULT_ROUTE_PLANNER_GPX_FILE_URI,
                    lastModified = LocalDateTime.of(2023, 6, 2, 16, 0),
                    travelTime = 5.hours,
                    distance = 10000,
                    incline = 1000,
                    decline = 1000,
                    waypointCount = 0,
                )
            ),
            externalGpxList = listOf(
                GpxHistoryItem(
                    name = "dera_szurdok.gpx",
                    fileUri = DEFAULT_EXTERNAL_GPX_FILE_URI,
                    lastModified = LocalDateTime.of(2023, 6, 3, 16, 0),
                    travelTime = Duration.ZERO,
                    distance = 0,
                    incline = 0,
                    decline = 0,
                    waypointCount = 100,
                )
            )
        )
    }

}

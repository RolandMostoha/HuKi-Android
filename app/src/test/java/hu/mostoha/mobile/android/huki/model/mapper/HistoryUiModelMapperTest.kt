package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.HistoryInfo
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.ui.home.history.place.PlaceHistoryAdapterModel
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

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
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places.first())))
                .plus(
                    PlaceHistoryAdapterModel.Header(
                        dateText = R.string.default_date_yesterday.toMessage(),
                    )
                )
                .plus(PlaceHistoryAdapterModel.Item(placeMapper.mapHistoryPlace(places.last())))
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
    }

}

package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_POST_CODE
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderItem
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MAX_HISTORY_ITEM
import org.junit.Test

class PlaceFinderUiModelMapperTest {

    private val hikingRouteRelationMapper = HikingRouteRelationMapper()
    private val placeMapper = PlaceDomainUiMapper(hikingRouteRelationMapper)
    private val mapper = PlaceFinderUiModelMapper(placeMapper)

    @Test
    fun `Given empty places, when map history items, then empty place finder items return`() {
        val places = emptyList<Place>()
        val placeFinderFeature = PlaceFinderFeature.MAP

        val placeFinderItems = mapper.mapHistoryItems(placeFinderFeature, places, null)

        assertThat(placeFinderItems).isEqualTo(emptyList<PlaceFinderItem>())
    }

    @Test
    fun `Given MAP feature with places, when map history items, then empty place finder items return`() {
        val places = listOf(DEFAULT_PLACE_WAY)
        val placeFinderFeature = PlaceFinderFeature.MAP

        val placeFinderItems = mapper.mapHistoryItems(placeFinderFeature, places, null)

        assertThat(placeFinderItems).isEqualTo(
            listOf(
                PlaceFinderItem.Place(placeMapper.mapHistoryPlace(DEFAULT_PLACE_WAY, null))
            )
        )
    }

    @Test
    fun `Given MAP feature with more places than max, when map history items, then only max items and sohw more return`() {
        val places = (1..PLACE_FINDER_MAX_HISTORY_ITEM + 10)
            .map { DEFAULT_PLACE_WAY.copy(it.toString()) }
        val placeFinderFeature = PlaceFinderFeature.MAP

        val placeFinderItems = mapper.mapHistoryItems(placeFinderFeature, places, null)

        assertThat(placeFinderItems.size).isEqualTo(PLACE_FINDER_MAX_HISTORY_ITEM + 1)
        assertThat(placeFinderItems.last()).isEqualTo(PlaceFinderItem.ShowMoreHistory)
    }

    @Test
    fun `Given ROUTE_PLANNER feature with more places than max, when map history items, then only max items return`() {
        val places = (1..PLACE_FINDER_MAX_HISTORY_ITEM + 10)
            .map { DEFAULT_PLACE_WAY.copy(it.toString()) }
        val placeFinderFeature = PlaceFinderFeature.ROUTE_PLANNER

        val placeFinderItems = mapper.mapHistoryItems(placeFinderFeature, places, null)

        assertThat(placeFinderItems.size).isEqualTo(PLACE_FINDER_MAX_HISTORY_ITEM)
    }

    @Test
    fun `Given DomainException, when mapPlacesErrorItem, then proper error item returns`() {
        val domainException = DomainException(R.string.error_message_too_many_requests.toMessage())

        val errorItem = mapper.mapPlacesErrorItem(domainException)

        assertThat(errorItem).isEqualTo(
            listOf(
                PlaceFinderItem.Info(
                    messageRes = domainException.messageRes,
                    drawableRes = R.drawable.ic_search_bar_error
                )
            )
        )
    }

    companion object {
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            name = DEFAULT_WAY_NAME,
            placeType = PlaceType.WAY,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE),
            boundingBox = BoundingBox(
                north = DEFAULT_WAY_EXTENT_NORTH,
                east = DEFAULT_WAY_EXTENT_EAST,
                south = DEFAULT_WAY_EXTENT_SOUTH,
                west = DEFAULT_WAY_EXTENT_WEST
            ),
            address = "$DEFAULT_WAY_POST_CODE $DEFAULT_WAY_CITY",
            placeFeature = PlaceFeature.MAP_SEARCH,
        )
    }

}

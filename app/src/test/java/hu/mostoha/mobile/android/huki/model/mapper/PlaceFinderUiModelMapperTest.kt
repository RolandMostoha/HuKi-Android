package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_COUNTRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_EXTENT_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_POST_CODE
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderItem
import hu.mostoha.mobile.android.huki.util.BUDAPEST_LOCATION
import hu.mostoha.mobile.android.huki.util.distanceBetween
import org.junit.Test

class PlaceFinderUiModelMapperTest {

    private val mapper = PlaceFinderUiModelMapper()

    @Test
    fun `Given empty place domain models, when mapPlaceFinderItems, then error item returns`() {
        val places = emptyList<Place>()

        val placeFinderItems = mapper.mapPlaceFinderItems(places)

        assertThat(placeFinderItems).isEqualTo(
            listOf(
                PlaceFinderItem.Error(
                    messageRes = R.string.place_finder_empty_message.toMessage(),
                    drawableRes = R.drawable.ic_search_bar_empty_result
                )
            )
        )
    }

    @Test
    fun `Given place domain models, when mapPlaceFinderItems, then correct list of search bar items return`() {
        val places = listOf(DEFAULT_PLACE_WAY)

        val placeFinderItems = mapper.mapPlaceFinderItems(places)

        assertThat(placeFinderItems).isEqualTo(
            listOf(
                PlaceFinderItem.Place(
                    PlaceUiModel(
                        osmId = DEFAULT_PLACE_WAY.osmId,
                        placeType = PlaceType.WAY,
                        primaryText = DEFAULT_PLACE_WAY.name.toMessage(),
                        secondaryText = Message.Text("${DEFAULT_PLACE_WAY.postCode} ${DEFAULT_PLACE_WAY.city}"),
                        iconRes = R.drawable.ic_home_search_bar_type_way,
                        geoPoint = DEFAULT_PLACE_WAY.location.toGeoPoint(),
                        boundingBox = DEFAULT_PLACE_WAY.boundingBox,
                        isLandscape = false
                    )
                )
            )
        )
    }

    @Test
    fun `Given place domain models without city, when mapPlaceFinderItems, then secondaryText contains the country`() {
        val places = listOf(DEFAULT_PLACE_WAY.copy(city = null))

        val placeFinderItems = mapper.mapPlaceFinderItems(places)

        assertThat(placeFinderItems).isEqualTo(
            listOf(
                PlaceFinderItem.Place(
                    PlaceUiModel(
                        osmId = DEFAULT_PLACE_WAY.osmId,
                        placeType = PlaceType.WAY,
                        primaryText = DEFAULT_PLACE_WAY.name.toMessage(),
                        secondaryText = Message.Text("${DEFAULT_PLACE_WAY.postCode} ${DEFAULT_PLACE_WAY.country}"),
                        iconRes = R.drawable.ic_home_search_bar_type_way,
                        geoPoint = DEFAULT_PLACE_WAY.location.toGeoPoint(),
                        boundingBox = DEFAULT_PLACE_WAY.boundingBox,
                        isLandscape = false
                    )
                )
            )
        )
    }

    @Test
    fun `Given place domain models with location, when mapPlaceFinderItems, then correct list of search bar items return`() {
        val places = listOf(DEFAULT_PLACE_WAY)
        val location = BUDAPEST_LOCATION

        val placeFinderItems = mapper.mapPlaceFinderItems(places, location)

        assertThat(placeFinderItems).isEqualTo(
            listOf(
                PlaceFinderItem.StaticActions,
                PlaceFinderItem.Place(
                    PlaceUiModel(
                        osmId = DEFAULT_PLACE_WAY.osmId,
                        placeType = PlaceType.WAY,
                        primaryText = DEFAULT_PLACE_WAY.name.toMessage(),
                        secondaryText = Message.Text("${DEFAULT_PLACE_WAY.postCode} ${DEFAULT_PLACE_WAY.city}"),
                        iconRes = R.drawable.ic_home_search_bar_type_way,
                        geoPoint = DEFAULT_PLACE_WAY.location.toGeoPoint(),
                        boundingBox = DEFAULT_PLACE_WAY.boundingBox,
                        isLandscape = false,
                        distanceText = DistanceFormatter.format(DEFAULT_PLACE_WAY.location.distanceBetween(location))
                    )
                )
            )
        )
    }

    @Test
    fun `Given DomainException, when mapPlacesErrorItem, then proper error item returns`() {
        val domainException = DomainException(R.string.error_message_too_many_requests.toMessage())

        val errorItem = mapper.mapPlacesErrorItem(domainException)

        assertThat(errorItem).isEqualTo(
            listOf(
                PlaceFinderItem.Error(
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
            country = DEFAULT_WAY_COUNTRY,
            postCode = DEFAULT_WAY_POST_CODE,
            city = DEFAULT_WAY_CITY,
            street = null
        )
    }

}

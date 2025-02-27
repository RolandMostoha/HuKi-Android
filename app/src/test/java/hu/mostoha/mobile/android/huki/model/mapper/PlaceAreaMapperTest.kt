package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_AREA_BOX
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_AREA_LOCATION
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_PROFILE
import hu.mostoha.mobile.android.huki.util.areaDistanceMessage
import org.junit.Test

class PlaceAreaMapperTest {

    @Test
    fun `Given location without place profile, when map location, then place address returns with formatted location`() {
        val location = LOCATION
        val boundingBox = DEFAULT_PLACE_AREA_BOX
        val placeProfile = null

        val placeArea = PlaceAreaMapper.map(location, boundingBox, placeProfile)

        assertThat(placeArea).isEqualTo(
            PlaceArea(
                placeAreaType = PlaceAreaType.MAP_SEARCH,
                location = location,
                boundingBox = boundingBox,
                addressMessage = LocationFormatter.formatText(location),
                distanceMessage = boundingBox.areaDistanceMessage(),
                iconRes = R.drawable.ic_place_category_city
            )
        )
    }

    @Test
    fun `Given place with country threshold, when map location, then place address returns with country`() {
        val location = DEFAULT_PLACE_AREA_LOCATION
        val boundingBox = BOUNDING_BOX_COUNTRY
        val placeProfile = DEFAULT_PLACE_PROFILE
        val placeArea = PlaceAreaMapper.map(location, boundingBox, placeProfile)

        assertThat(placeArea).isEqualTo(
            PlaceArea(
                placeAreaType = PlaceAreaType.MAP_SEARCH,
                location = location,
                boundingBox = boundingBox,
                addressMessage = DEFAULT_PLACE_PROFILE.address.country!!.toMessage(),
                distanceMessage = boundingBox.areaDistanceMessage(),
                iconRes = R.drawable.ic_place_category_city
            )
        )
    }

    @Test
    fun `Given place with city threshold, when map location, then place address returns with city`() {
        val location = DEFAULT_PLACE_AREA_LOCATION
        val boundingBox = BOUNDING_BOX_CITY
        val placeProfile = DEFAULT_PLACE_PROFILE
        val placeArea = PlaceAreaMapper.map(location, boundingBox, placeProfile)

        assertThat(placeArea).isEqualTo(
            PlaceArea(
                placeAreaType = PlaceAreaType.MAP_SEARCH,
                location = location,
                boundingBox = boundingBox,
                addressMessage = DEFAULT_PLACE_PROFILE.address.city!!.toMessage(),
                distanceMessage = boundingBox.areaDistanceMessage(),
                iconRes = R.drawable.ic_place_category_city
            )
        )
    }

    @Test
    fun `Given place with name threshold, when map location, then place address returns with name`() {
        val location = DEFAULT_PLACE_AREA_LOCATION
        val boundingBox = BOUNDING_BOX_NAME
        val placeProfile = DEFAULT_PLACE_PROFILE
        val placeArea = PlaceAreaMapper.map(location, boundingBox, placeProfile)

        assertThat(placeArea).isEqualTo(
            PlaceArea(
                placeAreaType = PlaceAreaType.MAP_SEARCH,
                location = location,
                boundingBox = boundingBox,
                addressMessage = DEFAULT_PLACE_PROFILE.displayName.toMessage(),
                distanceMessage = boundingBox.areaDistanceMessage(),
                iconRes = R.drawable.ic_place_category_city
            )
        )
    }

    companion object {
        private val BOUNDING_BOX_COUNTRY = BoundingBox(
            north = 48.95259,
            east = 19.77046,
            south = 46.53396,
            west = 18.06181
        )
        private val BOUNDING_BOX_CITY = BoundingBox(
            north = 47.73528,
            east = 18.90676,
            south = 47.70109,
            west = 18.88262,
        )
        private val BOUNDING_BOX_NAME = BoundingBox(
            north = 47.72702,
            east = 18.90159,
            south = 47.71299,
            west = 18.89168,
        )
        private val LOCATION = Location(47.71818436764824, 18.894693481226184)
    }

}

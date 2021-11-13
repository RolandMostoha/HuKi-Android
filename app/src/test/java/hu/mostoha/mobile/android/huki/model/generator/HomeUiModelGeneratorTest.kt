package hu.mostoha.mobile.android.huki.model.generator

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.generator.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.utils.Message
import io.mockk.mockk
import org.junit.Test

class HomeUiModelGeneratorTest {

    private val distanceFormatter = mockk<DistanceFormatter>()

    private val generator = HomeUiModelGenerator(distanceFormatter)

    @Test
    fun `Given domain places, when generatePlaceUiModels, then correct list of PlaceUiModel returns`() {
        val places = listOf(DEFAULT_PLACE)

        val placeUiModels = generator.generatePlaceUiModels(places)

        assertThat(
            listOf(
                PlaceUiModel(
                    osmId = DEFAULT_PLACE.osmId,
                    placeType = PlaceType.WAY,
                    primaryText = DEFAULT_PLACE.name,
                    secondaryText = Message.Text("${DEFAULT_PLACE.postCode} ${DEFAULT_PLACE.city}"),
                    iconRes = R.drawable.ic_home_search_bar_type_way
                )
            )
        ).isEqualTo(placeUiModels)
    }

    @Test
    fun `Given place without city, when generatePlaceUiModels, then secondaryText contains the country`() {
        val places = listOf(DEFAULT_PLACE.copy(city = null))

        val placeUiModels = generator.generatePlaceUiModels(places)

        assertThat(placeUiModels).isEqualTo(
            listOf(
                PlaceUiModel(
                    osmId = DEFAULT_PLACE.osmId,
                    placeType = PlaceType.WAY,
                    primaryText = DEFAULT_PLACE.name,
                    secondaryText = Message.Text("${DEFAULT_PLACE.postCode} ${DEFAULT_PLACE.country}"),
                    iconRes = R.drawable.ic_home_search_bar_type_way
                )
            )
        )
    }

    companion object {
        private val DEFAULT_PLACE = Place(
            osmId = "193407756L",
            name = "Széchenyi út",
            placeType = PlaceType.WAY,
            location = Location(47.0983397, 17.7575106),
            boundingBox = BoundingBox(
                north = 19.1130386,
                east = 47.5452098,
                south = 19.1125605,
                west = 47.544937
            ),
            country = "Magyarország",
            district = "Rákospalota",
            postCode = "1155",
            city = "Budapest",
            street = null
        )
    }

}

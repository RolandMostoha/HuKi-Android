package hu.mostoha.mobile.android.huki.model.generator

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.photon.*
import org.junit.Test

class PlacesDomainModelGeneratorTest {

    private val generator = PlacesDomainModelGenerator()

    @Test
    fun `Given photon query response, when generatePlace, then correct Place list returns`() {
        val photonQueryResponse = PhotonQueryResponse(
            features = listOf(DEFAULT_FEATURE_ITEM),
            type = "FeatureCollection"
        )

        val places = generator.generatePlace(photonQueryResponse)

        assertThat(places).isEqualTo(
            listOf(
                Place(
                    osmId = DEFAULT_FEATURE_ITEM.properties.osmId.toString(),
                    name = DEFAULT_FEATURE_ITEM.properties.name,
                    placeType = PlaceType.WAY,
                    location = Location(
                        DEFAULT_FEATURE_ITEM.geometry.coordinates[1],
                        DEFAULT_FEATURE_ITEM.geometry.coordinates[0]
                    ),
                    boundingBox = BoundingBox(
                        north = DEFAULT_FEATURE_ITEM.properties.extent!![2],
                        east = DEFAULT_FEATURE_ITEM.properties.extent!![1],
                        south = DEFAULT_FEATURE_ITEM.properties.extent!![0],
                        west = DEFAULT_FEATURE_ITEM.properties.extent!![3]
                    ),
                    country = DEFAULT_FEATURE_ITEM.properties.country,
                    county = DEFAULT_FEATURE_ITEM.properties.county,
                    district = DEFAULT_FEATURE_ITEM.properties.district,
                    postCode = DEFAULT_FEATURE_ITEM.properties.postCode,
                    city = DEFAULT_FEATURE_ITEM.properties.city,
                    street = DEFAULT_FEATURE_ITEM.properties.street
                )
            )
        )
    }

    companion object {
        private val DEFAULT_FEATURE_ITEM = FeaturesItem(
            geometry = Geometry(
                coordinates = listOf(17.7575106, 47.0983397),
                type = "Feature"
            ),
            type = "Feature",
            properties = Properties(
                osmId = 193407756L,
                osmType = OsmType.WAY,
                osmKey = "highway",
                osmValue = "tertiary",
                country = "Magyarország",
                city = "Budapest",
                postCode = "1155",
                county = "XIV. kerület",
                district = "Rákospalota",
                name = "Széchenyi út",
                state = "Central Hungary",
                extent = listOf(19.1125605, 47.5452098, 19.1130386, 47.544937),
                street = "Széchenyi út"
            )
        )
    }

}

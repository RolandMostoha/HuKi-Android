package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.photon.FeaturesItem
import hu.mostoha.mobile.android.huki.model.network.photon.OsmType
import hu.mostoha.mobile.android.huki.model.network.photon.PhotonGeometry
import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import hu.mostoha.mobile.android.huki.model.network.photon.Properties
import org.junit.Test

class GeocodingDomainModelMapperTest {

    private val mapper = GeocodingDomainModelMapper()

    @Test
    fun `Given photon query response, when map place, then correct Place list returns`() {
        val photonQueryResponse = PhotonQueryResponse(
            features = listOf(DEFAULT_PHOTON_FEATURE_ITEM),
            type = "FeatureCollection"
        )
        val expectedProperties = DEFAULT_PHOTON_FEATURE_ITEM.properties

        val places = mapper.mapPlace(photonQueryResponse)

        assertThat(places).isEqualTo(
            listOf(
                Place(
                    osmId = expectedProperties.osmId.toString(),
                    name = expectedProperties.name!!,
                    placeType = PlaceType.WAY,
                    location = Location(
                        DEFAULT_PHOTON_FEATURE_ITEM.geometry.coordinates[1],
                        DEFAULT_PHOTON_FEATURE_ITEM.geometry.coordinates[0]
                    ),
                    boundingBox = BoundingBox(
                        north = expectedProperties.extent!![1],
                        east = expectedProperties.extent!![2],
                        south = expectedProperties.extent!![3],
                        west = expectedProperties.extent!![0]
                    ),
                    country = expectedProperties.country,
                    county = expectedProperties.county,
                    district = expectedProperties.district,
                    postCode = expectedProperties.postCode,
                    city = expectedProperties.city,
                    street = "${expectedProperties.street} ${expectedProperties.houseNumber}"
                )
            )
        )
    }

    @Test
    fun `Given a feature item without name, when map place, then place returns with name of the street`() {
        val photonQueryResponse = PhotonQueryResponse(
            features = listOf(
                DEFAULT_PHOTON_FEATURE_ITEM.copy(
                    properties = DEFAULT_PHOTON_FEATURE_ITEM.properties.copy(
                        name = null
                    )
                )
            ),
            type = "FeatureCollection"
        )

        val places = mapper.mapPlace(photonQueryResponse)

        assertThat(places.first().name).isEqualTo("Széchenyi út 11")
    }

    @Test
    fun `Given a feature item without name and street, when map place, then place returns with name of the city`() {
        val photonQueryResponse = PhotonQueryResponse(
            features = listOf(
                DEFAULT_PHOTON_FEATURE_ITEM.copy(
                    properties = DEFAULT_PHOTON_FEATURE_ITEM.properties.copy(
                        name = null,
                        street = null,
                        houseNumber = null,
                    )
                )
            ),
            type = "FeatureCollection"
        )

        val places = mapper.mapPlace(photonQueryResponse)

        assertThat(places.first().name).isEqualTo("Budapest")
    }

    @Test
    fun `Given a feature item without name and street and city, when map place, then empty list returns`() {
        val photonQueryResponse = PhotonQueryResponse(
            features = listOf(
                DEFAULT_PHOTON_FEATURE_ITEM.copy(
                    properties = DEFAULT_PHOTON_FEATURE_ITEM.properties.copy(
                        name = null,
                        street = null,
                        houseNumber = null,
                        city = null,
                    )
                )
            ),
            type = "FeatureCollection"
        )

        val places = mapper.mapPlace(photonQueryResponse)

        assertThat(places).isEmpty()
    }

    companion object {
        private val DEFAULT_PHOTON_FEATURE_ITEM = FeaturesItem(
            geometry = PhotonGeometry(
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
                street = "Széchenyi út",
                houseNumber = "11"
            )
        )
    }

}

package hu.mostoha.mobile.android.huki.model.generator

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.network.overpass.*
import hu.mostoha.mobile.android.huki.model.network.photon.*
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import hu.mostoha.mobile.android.huki.util.calculateDistance
import org.junit.Assert.assertThrows
import org.junit.Test

class PlacesDomainModelGeneratorTest {

    private val generator = PlacesDomainModelGenerator()

    @Test
    fun `Given photon query response, when generatePlace, then correct Place list returns`() {
        val photonQueryResponse = PhotonQueryResponse(
            features = listOf(DEFAULT_PHOTON_FEATURE_ITEM),
            type = "FeatureCollection"
        )
        val expectedProperties = DEFAULT_PHOTON_FEATURE_ITEM.properties

        val places = generator.generatePlace(photonQueryResponse)

        assertThat(places).isEqualTo(
            listOf(
                Place(
                    osmId = expectedProperties.osmId.toString(),
                    name = expectedProperties.name,
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
    fun `Given overpass query response, when generateGeometryByNode, then node Geometry returns`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_NODE.id.toString()
        val photonQueryResponse = OverpassQueryResponse(listOf(DEFAULT_OVERPASS_ELEMENT_NODE))

        val geometry = generator.generateGeometryByNode(photonQueryResponse, osmId)

        assertThat(geometry).isEqualTo(
            Geometry.Node(
                osmId = osmId,
                location = Location(DEFAULT_OVERPASS_ELEMENT_NODE.lat!!, DEFAULT_OVERPASS_ELEMENT_NODE.lon!!)
            )
        )
    }

    @Test
    fun `Given overpass query without elements, when generateGeometryByNode, then domain exception throws`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_NODE.id.toString()
        val photonQueryResponse = OverpassQueryResponse(emptyList())

        val exception = assertThrows(DomainException::class.java) {
            generator.generateGeometryByNode(photonQueryResponse, osmId)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_missing_osm_id.toMessage())
    }

    @Test
    fun `Given overpass query response, when generateGeometryByWay, then way Geometry returns`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_WAY.id.toString()
        val photonQueryResponse = OverpassQueryResponse(listOf(DEFAULT_OVERPASS_ELEMENT_WAY))
        val expectedLocations = generator.extractLocations(DEFAULT_OVERPASS_ELEMENT_WAY.geometry!!)

        val geometry = generator.generateGeometryByWay(photonQueryResponse, osmId)

        assertThat(geometry).isEqualTo(
            Geometry.Way(
                osmId = osmId,
                locations = expectedLocations,
                distance = expectedLocations.calculateDistance()
            )
        )
    }

    @Test
    fun `Given overpass query without elements, when generateGeometryByWay, then domain exception throws`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_WAY.id.toString()
        val photonQueryResponse = OverpassQueryResponse(emptyList())

        val exception = assertThrows(DomainException::class.java) {
            generator.generateGeometryByWay(photonQueryResponse, osmId)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_missing_osm_id.toMessage())
    }

    @Test
    fun `Given overpass query response, when generateGeometryByRelation, then relation Geometry returns`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_RELATION.id.toString()
        val photonQueryResponse = OverpassQueryResponse(listOf(DEFAULT_OVERPASS_ELEMENT_RELATION))

        val geometry = generator.generateGeometryByRelation(photonQueryResponse, osmId)

        assertThat(geometry).isEqualTo(
            Geometry.Relation(
                osmId = osmId,
                ways = DEFAULT_OVERPASS_ELEMENT_RELATION.members!!.map {
                    val locations = generator.extractLocations(it.geometry!!)

                    Geometry.Way(
                        osmId = it.ref,
                        locations = locations,
                        distance = locations.calculateDistance()
                    )
                }
            )
        )
    }

    @Test
    fun `Given overpass query without elements, when generateGeometryByRelation, then domain exception throws`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_RELATION.id.toString()
        val photonQueryResponse = OverpassQueryResponse(emptyList())

        val exception = assertThrows(DomainException::class.java) {
            generator.generateGeometryByRelation(photonQueryResponse, osmId)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_missing_osm_id.toMessage())
    }

    @Test
    fun `Given overpass query response, when generateHikingRoutes, then HikingRoute list returns`() {
        val photonQueryResponse = OverpassQueryResponse(
            listOf(
                DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE,
                Element(
                    id = 12345L,
                    type = ElementType.RELATION,
                    tags = Tags(
                        name = null,
                        jel = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
                    )
                )
            )
        )

        val hikingRoutes = generator.generateHikingRoutes(photonQueryResponse)

        assertThat(hikingRoutes).isEqualTo(
            listOf(
                HikingRoute(
                    osmId = DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE.id.toString(),
                    name = DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE.tags!!.name!!,
                    symbolType = DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE.tags!!.jel!!
                )
            )
        )
    }

    @Test
    fun `Given overpass geom list, when extractLocations, then Location list returns`() {
        val geometries = listOf(
            Geom(null, null),
            Geom(null, 19.12345),
            Geom(47.12345, null),
            Geom(47.12345, 19.12345)
        )

        val locations = generator.extractLocations(geometries)

        assertThat(locations).isEqualTo(listOf(Location(47.12345, 19.12345)))
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
        private val DEFAULT_OVERPASS_ELEMENT_NODE = Element(
            id = DEFAULT_NODE_OSM_ID.toLong(),
            type = ElementType.NODE,
            lat = DEFAULT_NODE_LATITUDE,
            lon = DEFAULT_NODE_LONGITUDE
        )
        private val DEFAULT_OVERPASS_ELEMENT_WAY = Element(
            id = DEFAULT_WAY_OSM_ID.toLong(),
            type = ElementType.WAY,
            geometry = DEFAULT_WAY_GEOMETRY.map { Geom(it.first, it.second) }
        )
        private val DEFAULT_OVERPASS_ELEMENT_RELATION = Element(
            id = DEFAULT_RELATION_OSM_ID.toLong(),
            type = ElementType.RELATION,
            members = listOf(
                Member(
                    ref = DEFAULT_RELATION_WAY_1_OSM_ID,
                    type = ElementType.WAY,
                    geometry = DEFAULT_RELATION_WAY_1_GEOMETRY.map { Geom(it.first, it.second) }
                ),
                Member(
                    ref = DEFAULT_RELATION_WAY_2_OSM_ID,
                    type = ElementType.WAY,
                    geometry = DEFAULT_RELATION_WAY_2_GEOMETRY.map { Geom(it.first, it.second) }
                )
            )
        )
        private val DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE = Element(
            id = DEFAULT_HIKING_ROUTE_OSM_ID.toLong(),
            type = ElementType.RELATION,
            tags = Tags(
                name = DEFAULT_HIKING_ROUTE_NAME,
                jel = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
            )
        )
    }

}

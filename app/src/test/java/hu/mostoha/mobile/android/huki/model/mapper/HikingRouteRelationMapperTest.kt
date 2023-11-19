package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import org.junit.Test
import org.osmdroid.util.GeoPoint

class HikingRouteRelationMapperTest {

    private val mapper = HikingRouteRelationMapper()

    @Test
    fun `Given open relation, when map, then relation UI model returns with waypoints`() {
        val relation = RELATION_OPEN

        val result = mapper.map(relation)

        assertThat(result).isEqualTo(
            GeometryUiModel.HikingRoute(
                ways = relation.ways.map { way ->
                    GeometryUiModel.Way(
                        osmId = way.osmId,
                        geoPoints = way.locations.toGeoPoints(),
                        isClosed = false,
                    )
                },
                isClosed = false,
                waypoints = listOf(
                    GeoPoint(47.469729, 19.043818),
                    GeoPoint(47.466759, 19.025542),
                ),
            )
        )
    }

    @Test
    fun `Given closed relation, when map, then relation UI model returns with waypoints`() {
        val relation = RELATION_CLOSED

        val result = mapper.map(relation)

        assertThat(result).isEqualTo(
            GeometryUiModel.HikingRoute(
                ways = relation.ways.map { way ->
                    GeometryUiModel.Way(
                        osmId = way.osmId,
                        geoPoints = way.locations.toGeoPoints(),
                        isClosed = false,
                    )
                },
                isClosed = true,
                waypoints = listOf(
                    GeoPoint(47.469729, 19.043818),
                ),
            )
        )
    }

    companion object {
        val RELATION_OPEN = Geometry.Relation(
            osmId = "12345",
            listOf(
                Geometry.Way(
                    osmId = "1",
                    locations = listOf(
                        Location(47.469729, 19.043818),
                        Location(47.469641, 19.052566),
                    ),
                    20
                ),
                Geometry.Way(
                    osmId = "3",
                    locations = listOf(
                        Location(47.458056, 19.046322),
                        Location(47.459313, 19.017576),
                    ),
                    20
                ),
                Geometry.Way(
                    osmId = "4",
                    locations = listOf(
                        Location(47.459313, 19.017576),
                        Location(47.466759, 19.025542),
                    ),
                    20
                ),
                Geometry.Way(
                    osmId = "2",
                    locations = listOf(
                        Location(47.469641, 19.052566),
                        Location(47.458056, 19.046322),
                    ),
                    20
                ),
            ),
        )
        val RELATION_CLOSED = Geometry.Relation(
            osmId = "123456",
            listOf(
                Geometry.Way(
                    osmId = "1",
                    locations = listOf(
                        Location(47.469729, 19.043818),
                        Location(47.469641, 19.052566),
                    ),
                    20
                ),
                Geometry.Way(
                    osmId = "3",
                    locations = listOf(
                        Location(47.458056, 19.046322),
                        Location(47.459313, 19.017576),
                    ),
                    20
                ),
                Geometry.Way(
                    osmId = "4",
                    locations = listOf(
                        Location(47.459313, 19.017576),
                        Location(47.466759, 19.025542),
                    ),
                    20
                ),
                Geometry.Way(
                    osmId = "2",
                    locations = listOf(
                        Location(47.469641, 19.052566),
                        Location(47.458056, 19.046322),
                        Location(47.469729, 19.043818),
                    ),
                    20
                ),
            ),
        )
    }

}

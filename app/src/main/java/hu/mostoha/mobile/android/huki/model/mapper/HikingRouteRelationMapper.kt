package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import javax.inject.Inject

class HikingRouteRelationMapper @Inject constructor() {

    fun map(relation: Geometry.Relation): GeometryUiModel.HikingRoute {
        val firstLocations = relation.ways.map { it.locations.first() }
        val lastLocations = relation.ways.map { it.locations.last() }
        val endLocations = (firstLocations + lastLocations).distinct()

        val repeatedLocationMap = relation.ways
            .flatMap { it.locations }
            .groupingBy { it }
            .eachCount()
            .filterKeys { it in endLocations }
            .filter { it.value > 1 }

        val isClosed = repeatedLocationMap.size >= relation.ways.size

        val wayPoints = if (isClosed) {
            listOf(relation.ways.first().locations.first())
        } else {
            endLocations - repeatedLocationMap.keys
        }

        return GeometryUiModel.HikingRoute(
            ways = relation.ways.map { way ->
                GeometryUiModel.Way(
                    osmId = way.osmId,
                    geoPoints = way.locations.toGeoPoints(),
                    isClosed = false,
                )
            },
            isClosed = isClosed,
            waypoints = wayPoints.toGeoPoints(),
        )
    }

}

package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.util.calculateCenter
import hu.mostoha.mobile.android.huki.util.distanceBetween
import org.osmdroid.util.GeoPoint
import java.util.UUID
import javax.inject.Inject

class PlaceDomainUiMapper @Inject constructor(
    private val hikingRouteRelationMapper: HikingRouteRelationMapper,
) {

    fun mapHistoryPlace(place: Place, myLocation: Location? = null): PlaceUiModel {
        return PlaceUiModel(
            osmId = place.osmId,
            placeType = place.placeType,
            primaryText = place.name.toMessage(),
            secondaryText = place.address.toMessage(),
            iconRes = if (place.historyInfo != null) {
                R.drawable.ic_place_type_history
            } else {
                when (place.placeType) {
                    PlaceType.NODE -> R.drawable.ic_place_type_node
                    PlaceType.WAY -> R.drawable.ic_place_type_way
                    PlaceType.RELATION, PlaceType.HIKING_ROUTE -> R.drawable.ic_place_type_relation
                }
            },
            geoPoint = place.location.toGeoPoint(),
            boundingBox = place.boundingBox,
            distanceText = myLocation?.let {
                DistanceFormatter.formatWithoutScale(place.location.distanceBetween(myLocation))
            },
            placeFeature = place.placeFeature,
            historyInfo = place.historyInfo,
        )
    }

    fun mapToPlaceDetailsUiModel(placeUiModel: PlaceUiModel, geometry: Geometry): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            placeUiModel = placeUiModel,
            geometryUiModel = if (placeUiModel.placeType == PlaceType.HIKING_ROUTE) {
                hikingRouteRelationMapper.map(geometry as Geometry.Relation)
            } else {
                mapGeometryUiModel(geometry)
            }
        )
    }

    fun mapToPlaceDetailsPreview(placeUiModel: PlaceUiModel): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            placeUiModel = placeUiModel.copy(
                iconRes = when (placeUiModel.placeType) {
                    PlaceType.NODE -> R.drawable.ic_place_type_node
                    PlaceType.WAY -> R.drawable.ic_place_type_way
                    PlaceType.RELATION, PlaceType.HIKING_ROUTE -> R.drawable.ic_place_type_relation
                }
            ),
            geometryUiModel = GeometryUiModel.Node(placeUiModel.geoPoint)
        )
    }

    fun mapToHikingRouteDetails(hikingRoute: HikingRouteUiModel, geometry: Geometry): PlaceDetailsUiModel {
        val relation = geometry as? Geometry.Relation ?: error("Hiking route can only be a Relation")

        val totalDistance = relation.ways.sumOf { it.distance }

        return mapToPlaceDetailsUiModel(
            placeUiModel = PlaceUiModel(
                osmId = hikingRoute.osmId,
                primaryText = hikingRoute.name.toMessage(),
                secondaryText = DistanceFormatter.format(totalDistance),
                placeType = PlaceType.HIKING_ROUTE,
                iconRes = hikingRoute.symbolIcon,
                geoPoint = relation.ways.flatMap { it.locations }
                    .calculateCenter()
                    .toGeoPoint(),
                boundingBox = null,
                placeFeature = PlaceFeature.HIKING_ROUTE_WAYPOINT,
            ),
            geometry = geometry
        )
    }

    fun mapWayUiModel(osmId: String, way: Geometry.Way): GeometryUiModel.Way {
        val locations = way.locations

        return GeometryUiModel.Way(
            osmId = osmId,
            geoPoints = locations.toGeoPoints(),
            isClosed = locations.first() == locations.last()
        )
    }

    fun mapToPlaceUiModel(
        geoPoint: GeoPoint,
        placeFeature: PlaceFeature,
        geocodedPlace: Place? = null
    ): PlaceUiModel {
        return if (geocodedPlace != null) {
            PlaceUiModel(
                osmId = geocodedPlace.osmId,
                placeType = geocodedPlace.placeType,
                geoPoint = geoPoint,
                primaryText = geocodedPlace.name.toMessage(),
                secondaryText = geocodedPlace.address.toMessage(),
                iconRes = when (geocodedPlace.placeType) {
                    PlaceType.NODE -> R.drawable.ic_place_type_node
                    PlaceType.WAY -> R.drawable.ic_place_type_way
                    PlaceType.RELATION, PlaceType.HIKING_ROUTE -> R.drawable.ic_place_type_relation
                },
                placeFeature = geocodedPlace.placeFeature,
            )
        } else {
            PlaceUiModel(
                osmId = UUID.randomUUID().toString(),
                placeType = PlaceType.NODE,
                geoPoint = geoPoint,
                primaryText = LocationFormatter.formatText(geoPoint),
                secondaryText = when (placeFeature) {
                    PlaceFeature.MAP_MY_LOCATION -> R.string.place_details_my_location_text.toMessage()
                    PlaceFeature.MAP_PICKED_LOCATION -> R.string.place_details_pick_location_text.toMessage()
                    PlaceFeature.ROUTE_PLANNER_MY_LOCATION -> R.string.place_details_my_location_text.toMessage()
                    PlaceFeature.ROUTE_PLANNER_PICKED_LOCATION -> R.string.place_details_pick_location_text.toMessage()
                    PlaceFeature.OKT_WAYPOINT -> R.string.place_details_okt_waypoint_text.toMessage()
                    PlaceFeature.GPX_WAYPOINT -> R.string.place_details_gpx_waypoint_text.toMessage()
                    else -> R.string.place_details_coordinates_text.toMessage()
                },
                iconRes = R.drawable.ic_place_type_node,
                placeFeature = placeFeature,
            )
        }
    }

    private fun mapGeometryUiModel(geometry: Geometry): GeometryUiModel {
        return when (geometry) {
            is Geometry.Node -> GeometryUiModel.Node(geometry.location.toGeoPoint())
            is Geometry.Way -> mapWayUiModel(geometry.osmId, geometry)
            is Geometry.Relation -> {
                GeometryUiModel.Relation(
                    ways = geometry.ways.map { mapWayUiModel(geometry.osmId, it) }
                )
            }
        }
    }

}
package hu.mostoha.mobile.android.huki.model.mapper

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceRequestType
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.util.calculateCenter
import org.osmdroid.util.GeoPoint
import java.util.UUID
import javax.inject.Inject

class HomeUiModelMapper @Inject constructor() {

    fun mapPlaceDetails(placeUiModel: PlaceUiModel): PlaceDetailsUiModel {
        return mapPlaceDetails(
            placeUiModel = placeUiModel,
            geometry = Geometry.Node(placeUiModel.osmId, placeUiModel.geoPoint.toLocation())
        )
    }

    fun mapPlaceDetails(placeUiModel: PlaceUiModel, geometry: Geometry): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            placeUiModel = placeUiModel,
            geometryUiModel = if (placeUiModel.placeType == PlaceType.HIKING_ROUTE) {
                HikingRouteRelationMapper.map(geometry as Geometry.Relation)
            } else {
                mapGeometryUiModel(geometry)
            }
        )
    }

    fun mapPlaceDetails(geoPoint: GeoPoint, placeRequestType: PlaceRequestType, place: Place?): PlaceDetailsUiModel {
        return if (place != null) {
            PlaceDetailsUiModel(
                placeUiModel = PlaceUiModel(
                    osmId = place.osmId,
                    placeType = place.placeType,
                    primaryText = place.name.toMessage(),
                    secondaryText = listOfNotNull(
                        place.postCode,
                        place.city ?: place.country,
                        place.street
                    ).joinToString(" ").toMessage(),
                    iconRes = R.drawable.ic_place_type_node,
                    geoPoint = geoPoint,
                ),
                geometryUiModel = GeometryUiModel.Node(geoPoint),
            )
        } else {
            PlaceDetailsUiModel(
                placeUiModel = PlaceUiModel(
                    osmId = UUID.randomUUID().toString(),
                    placeType = PlaceType.NODE,
                    geoPoint = geoPoint,
                    primaryText = LocationFormatter.formatText(geoPoint),
                    secondaryText = when (placeRequestType) {
                        PlaceRequestType.MY_LOCATION -> R.string.place_details_my_location_text.toMessage()
                        PlaceRequestType.PICKED_LOCATION -> R.string.place_details_pick_location_text.toMessage()
                        PlaceRequestType.OKT_WAYPOINT -> R.string.place_details_okt_waypoint_text.toMessage()
                        PlaceRequestType.GPX_WAYPOINT -> R.string.place_details_gpx_waypoint_text.toMessage()
                    },
                    iconRes = R.drawable.ic_place_type_node,
                ),
                geometryUiModel = GeometryUiModel.Node(geoPoint),
            )
        }
    }

    fun mapLandscapes(landscapes: List<Landscape>): List<LandscapeUiModel> {
        return landscapes.map { landscape ->
            LandscapeUiModel(
                osmId = landscape.osmId,
                osmType = landscape.osmType,
                name = landscape.nameRes.toMessage(),
                geoPoint = landscape.center.toGeoPoint(),
                iconRes = getLandscapeIcon(landscape),
                markerRes = getMarkerIcon(landscape),
            )
        }
    }

    fun mapLandscapeDetails(landscapeUiModel: LandscapeUiModel, geometry: Geometry): LandscapeDetailsUiModel {
        return LandscapeDetailsUiModel(
            landscapeUiModel = landscapeUiModel,
            geometryUiModel = when (geometry) {
                is Geometry.Way ->
                    GeometryUiModel.Relation(
                        ways = listOf(mapWayUiModel(geometry.osmId, geometry))
                    )
                is Geometry.Relation -> {
                    GeometryUiModel.Relation(
                        ways = geometry.ways.map { mapWayUiModel(geometry.osmId, it) }
                    )
                }
                else -> throw IllegalArgumentException("Node geometry is not allowed for landscapes: $geometry")
            },
        )
    }

    fun mapHikingRoutes(placeName: String, hikingRoutes: List<HikingRoute>): List<HikingRoutesItem> {
        return if (hikingRoutes.isEmpty()) {
            mutableListOf<HikingRoutesItem>()
                .plus(HikingRoutesItem.Header(placeName))
                .plus(HikingRoutesItem.Empty)
        } else {
            mutableListOf<HikingRoutesItem>()
                .plus(HikingRoutesItem.Header(placeName))
                .plus(
                    hikingRoutes.map { hikingRoute ->
                        HikingRoutesItem.Item(
                            HikingRouteUiModel(
                                osmId = hikingRoute.osmId,
                                name = hikingRoute.name,
                                symbolIcon = hikingRoute.symbolType.getIconRes()
                            )
                        )
                    }
                )
        }
    }

    fun mapHikingRouteDetails(hikingRoute: HikingRouteUiModel, geometry: Geometry): PlaceDetailsUiModel {
        val relation = geometry as? Geometry.Relation ?: error("Hiking route can only be a Relation")

        val totalDistance = relation.ways.sumOf { it.distance }

        return mapPlaceDetails(
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
            ),
            geometry = geometry
        )
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

    private fun mapWayUiModel(osmId: String, way: Geometry.Way): GeometryUiModel.Way {
        val locations = way.locations

        return GeometryUiModel.Way(
            osmId = osmId,
            geoPoints = locations.toGeoPoints(),
            isClosed = locations.first() == locations.last()
        )
    }

    @DrawableRes
    private fun getLandscapeIcon(landscape: Landscape): Int {
        return when (landscape.landscapeType) {
            LandscapeType.MOUNTAIN_LOW -> R.drawable.ic_landscapes_mountain_low
            LandscapeType.MOUNTAIN_MEDIUM -> R.drawable.ic_landscapes_mountain_medium
            LandscapeType.MOUNTAIN_HIGH -> R.drawable.ic_landscapes_mountain_high
            LandscapeType.MOUNTAIN_WITH_LAKE -> R.drawable.ic_landscapes_lake
            LandscapeType.MOUNTAIN_WITH_CASTLE -> R.drawable.ic_landscapes_castle
            LandscapeType.CAVE_SYSTEM -> R.drawable.ic_landscapes_cave
            LandscapeType.WINE_AREA -> R.drawable.ic_landscapes_grape
            LandscapeType.STAR_GAZING_AREA -> R.drawable.ic_landscapes_telescope
            LandscapeType.FOREST_AREA -> R.drawable.ic_landscapes_forest
            LandscapeType.PLAIN_LAND -> R.drawable.ic_landscapes_plain_land
        }
    }

    @DrawableRes
    private fun getMarkerIcon(landscape: Landscape): Int {
        return when (landscape.landscapeType) {
            LandscapeType.MOUNTAIN_LOW -> R.drawable.ic_marker_landscapes_mountain_low
            LandscapeType.MOUNTAIN_MEDIUM -> R.drawable.ic_marker_landscapes_mountain_medium
            LandscapeType.MOUNTAIN_HIGH -> R.drawable.ic_marker_landscapes_mountain_high
            LandscapeType.MOUNTAIN_WITH_LAKE -> R.drawable.ic_marker_landscapes_lake
            LandscapeType.MOUNTAIN_WITH_CASTLE -> R.drawable.ic_marker_landscapes_castle
            LandscapeType.CAVE_SYSTEM -> R.drawable.ic_marker_landscapes_cave
            LandscapeType.WINE_AREA -> R.drawable.ic_marker_landscapes_grape
            LandscapeType.STAR_GAZING_AREA -> R.drawable.ic_marker_landscapes_telescope
            LandscapeType.FOREST_AREA -> R.drawable.ic_marker_landscapes_forest
            LandscapeType.PLAIN_LAND -> R.drawable.ic_marker_landscapes_plain_land
        }
    }

}

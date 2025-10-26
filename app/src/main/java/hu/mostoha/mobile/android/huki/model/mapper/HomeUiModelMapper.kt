package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.resolveIcon
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import javax.inject.Inject

class HomeUiModelMapper @Inject constructor(
    private val placeMapper: PlaceDomainUiMapper,
) {

    fun mapLandscapes(landscapes: List<Landscape>): List<LandscapeUiModel> {
        return landscapes.map { mapLandscape(it) }
    }

    fun mapLandscape(landscape: Landscape): LandscapeUiModel = LandscapeUiModel(
        osmId = landscape.osmId,
        placeType = landscape.osmType,
        name = landscape.nameRes.toMessage(),
        geoPoint = landscape.center.toGeoPoint(),
        color = landscape.color,
        iconRes = landscape.landscapeType.resolveIcon(),
        destinations = landscape.destinations
    )

    fun mapLandscapeDetails(landscapeUiModel: LandscapeUiModel, geometry: Geometry): LandscapeDetailsUiModel {
        return LandscapeDetailsUiModel(
            landscapeUiModel = landscapeUiModel,
            geometryUiModel = when (geometry) {
                is Geometry.Way ->
                    GeometryUiModel.Relation(
                        ways = listOf(placeMapper.mapWayUiModel(geometry.osmId, geometry))
                    )
                is Geometry.Relation -> {
                    GeometryUiModel.Relation(
                        ways = geometry.ways.map { placeMapper.mapWayUiModel(geometry.osmId, it) }
                    )
                }
                else -> throw IllegalArgumentException("Node geometry is not allowed for landscapes: $geometry")
            },
            isSelected = false,
        )
    }

    fun mapLandscapesGeometry(landscapeGeometryList: List<Pair<Landscape, Geometry>>): List<LandscapeDetailsUiModel> {
        val landscapeDetailsUiModels = landscapeGeometryList.map { landscapeGeometryPair ->
            val landscapeUiModel = mapLandscape(landscapeGeometryPair.first)
            val geometry = landscapeGeometryPair.second

            mapLandscapeDetails(landscapeUiModel, geometry)
        }
        return landscapeDetailsUiModels
    }

    fun mapHikingRoutes(placeArea: PlaceArea, hikingRoutes: List<HikingRoute>): List<HikingRoutesItem> {
        return if (hikingRoutes.isEmpty()) {
            mutableListOf<HikingRoutesItem>()
                .plus(HikingRoutesItem.Header(placeArea))
                .plus(HikingRoutesItem.Empty)
        } else {
            mutableListOf<HikingRoutesItem>()
                .plus(HikingRoutesItem.Header(placeArea))
                .plus(
                    hikingRoutes.map { hikingRoute ->
                        HikingRoutesItem.Item(mapHikingRoute(hikingRoute))
                    }
                )
        }
    }

    fun mapHikingRoute(hikingRoute: HikingRoute): HikingRouteUiModel {
        return HikingRouteUiModel(
            osmId = hikingRoute.osmId,
            name = hikingRoute.name,
            symbolIcon = hikingRoute.symbolType.iconRes
        )
    }

}

package hu.mostoha.mobile.android.huki.model.mapper

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
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
        return landscapes.map { landscape ->
            LandscapeUiModel(
                osmId = landscape.osmId,
                osmType = landscape.osmType,
                name = landscape.nameRes.toMessage(),
                geoPoint = landscape.center.toGeoPoint(),
                iconRes = getLandscapeIcon(landscape)
            )
        }
    }

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
        )
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
                        HikingRoutesItem.Item(
                            HikingRouteUiModel(
                                osmId = hikingRoute.osmId,
                                name = hikingRoute.name,
                                symbolIcon = hikingRoute.symbolType.iconRes
                            )
                        )
                    }
                )
        }
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

}

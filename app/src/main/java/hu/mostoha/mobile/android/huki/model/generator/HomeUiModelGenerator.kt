package hu.mostoha.mobile.android.huki.model.generator

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatShortDate
import hu.mostoha.mobile.android.huki.extensions.toLocalDateTime
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import java.io.File
import javax.inject.Inject

class HomeUiModelGenerator @Inject constructor(
    @ApplicationContext val context: Context,
    private val distanceFormatter: DistanceFormatter
) {

    fun generatePlacesResult(predictions: List<PlacePrediction>): List<PlaceUiModel> {
        return predictions.map {
            PlaceUiModel(
                id = it.id,
                placeType = it.placeType,
                primaryText = it.primaryText,
                secondaryText = it.secondaryText,
                iconRes = when (it.placeType) {
                    PlaceType.NODE -> R.drawable.ic_home_search_bar_type_node
                    PlaceType.WAY -> R.drawable.ic_home_search_bar_type_way
                    PlaceType.RELATION -> R.drawable.ic_home_search_bar_type_relation
                }
            )
        }
    }

    fun generatePlaceDetails(placeUiModel: PlaceUiModel, place: PlaceDetails): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            id = place.id,
            place = placeUiModel,
            payLoad = when (place.payLoad) {
                is PayLoad.Node -> {
                    UiPayLoad.Node(place.payLoad.location.toGeoPoint())
                }
                is PayLoad.Way -> {
                    val locations = place.payLoad.locations
                    UiPayLoad.Way(
                        id = place.id,
                        geoPoints = locations.map { it.toGeoPoint() },
                        isClosed = locations.first() == locations.last()
                    )
                }
                is PayLoad.Relation -> {
                    UiPayLoad.Relation(
                        ways = place.payLoad.ways.map { way ->
                            val locations = way.locations
                            UiPayLoad.Way(
                                id = place.id,
                                geoPoints = locations.map { it.toGeoPoint() },
                                isClosed = locations.first() == locations.last()
                            )
                        }
                    )
                }
            }
        )
    }

    fun generateLandscapes(landscapes: List<Landscape>): List<PlaceUiModel> {
        return landscapes.map {
            PlaceUiModel(
                id = it.id,
                placeType = PlaceType.WAY,
                primaryText = it.name,
                secondaryText = context.getString(R.string.home_bottom_sheet_landscape_secondary),
                iconRes = when (it.type) {
                    LandscapeType.MOUNTAIN_RANGE_LOW -> R.drawable.ic_landscapes_mountain_low
                    LandscapeType.MOUNTAIN_RANGE_HIGH -> R.drawable.ic_landscapes_mountain_high
                    LandscapeType.PLATEAU_WITH_WATER -> R.drawable.ic_landscapes_water
                    LandscapeType.CAVE_SYSTEM -> R.drawable.ic_landscapes_cave
                }
            )
        }
    }

    fun generateHikingRoutes(placeName: String, hikingRoutes: List<HikingRoute>): List<HikingRoutesItem> {
        return mutableListOf<HikingRoutesItem>()
            .plus(HikingRoutesItem.Header(placeName))
            .plus(hikingRoutes.map {
                HikingRoutesItem.Item(
                    HikingRouteUiModel(
                        id = it.id,
                        name = it.name,
                        symbolIcon = it.symbolType.getIconRes()
                    )
                )
            })
    }

    fun generateHikingRouteDetails(hikingRoute: HikingRouteUiModel, placeDetails: PlaceDetails): PlaceDetailsUiModel {
        val relation = placeDetails.payLoad as PayLoad.Relation
        val totalDistance = relation.ways.sumOf { it.distance }
        return generatePlaceDetails(
            placeUiModel = PlaceUiModel(
                id = hikingRoute.id,
                primaryText = hikingRoute.name,
                secondaryText = distanceFormatter.format(totalDistance),
                placeType = PlaceType.RELATION,
                iconRes = hikingRoute.symbolIcon
            ),
            place = placeDetails
        )
    }

    fun generateHikingLayerDetails(hikingLayerFile: File?): HikingLayerDetailsUiModel {
        return HikingLayerDetailsUiModel(
            isHikingLayerFileDownloaded = hikingLayerFile != null,
            hikingLayerFile = hikingLayerFile,
            lastUpdatedText = if (hikingLayerFile?.lastModified() != null) {
                hikingLayerFile.lastModified().toLocalDateTime().formatShortDate()
            } else {
                null
            }
        )
    }

}

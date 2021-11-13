package hu.mostoha.mobile.android.huki.model.generator

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatShortDate
import hu.mostoha.mobile.android.huki.extensions.toLocalDateTime
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.utils.Message
import hu.mostoha.mobile.android.huki.ui.utils.toMessage
import java.io.File
import javax.inject.Inject

class HomeUiModelGenerator @Inject constructor(private val distanceFormatter: DistanceFormatter) {

    fun generatePlaceUiModels(places: List<Place>): List<PlaceUiModel> {
        return places.map { place ->
            PlaceUiModel(
                osmId = place.osmId,
                placeType = place.placeType,
                primaryText = place.name,
                secondaryText = Message.Text(generateAddress(place)),
                iconRes = when (place.placeType) {
                    PlaceType.NODE -> R.drawable.ic_home_search_bar_type_node
                    PlaceType.WAY -> R.drawable.ic_home_search_bar_type_way
                    PlaceType.RELATION -> R.drawable.ic_home_search_bar_type_relation
                }
            )
        }
    }

    private fun generateAddress(place: Place): String {
        return listOfNotNull(
            place.postCode,
            place.city ?: place.country,
            place.street
        ).joinToString(" ")
    }

    fun generatePlaceDetails(placeUiModel: PlaceUiModel, place: PlaceDetails): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            osmId = place.osmId,
            placeUiModel = placeUiModel,
            payload = when (place.payload) {
                is Payload.Node -> {
                    UiPayload.Node(place.payload.location.toGeoPoint())
                }
                is Payload.Way -> {
                    val locations = place.payload.locations
                    UiPayload.Way(
                        osmId = place.osmId,
                        geoPoints = locations.map { it.toGeoPoint() },
                        isClosed = locations.first() == locations.last()
                    )
                }
                is Payload.Relation -> {
                    UiPayload.Relation(
                        ways = place.payload.ways.map { way ->
                            val locations = way.locations
                            UiPayload.Way(
                                osmId = place.osmId,
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
                osmId = it.osmId,
                placeType = PlaceType.WAY,
                primaryText = it.name,
                secondaryText = R.string.home_bottom_sheet_landscape_secondary.toMessage(),
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
                        osmId = it.osmId,
                        name = it.name,
                        symbolIcon = it.symbolType.getIconRes()
                    )
                )
            })
    }

    fun generateHikingRouteDetails(hikingRoute: HikingRouteUiModel, placeDetails: PlaceDetails): PlaceDetailsUiModel {
        val relation = placeDetails.payload as Payload.Relation
        val totalDistance = relation.ways.sumOf { it.distance }
        return generatePlaceDetails(
            placeUiModel = PlaceUiModel(
                osmId = hikingRoute.osmId,
                primaryText = hikingRoute.name,
                secondaryText = distanceFormatter.format(totalDistance).toMessage(),
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

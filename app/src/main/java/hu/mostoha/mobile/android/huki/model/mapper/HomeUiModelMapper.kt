package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.util.Message
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import hu.mostoha.mobile.android.huki.util.calculateCenter
import javax.inject.Inject

class HomeUiModelMapper @Inject constructor() {

    fun generateSearchBarItems(places: List<Place>): List<SearchBarItem> {
        return if (places.isEmpty()) {
            listOf(
                SearchBarItem.Error(
                    messageRes = R.string.search_bar_empty_message.toMessage(),
                    drawableRes = R.drawable.ic_search_bar_empty_result
                )
            )
        } else {
            generatePlaces(places).map { placeUiModel -> SearchBarItem.Place(placeUiModel) }
        }
    }

    private fun generatePlaces(places: List<Place>): List<PlaceUiModel> {
        return places.map { place ->
            PlaceUiModel(
                osmId = place.osmId,
                placeType = place.placeType,
                primaryText = place.name.toMessage(),
                secondaryText = Message.Text(generateAddress(place)),
                iconRes = when (place.placeType) {
                    PlaceType.NODE -> R.drawable.ic_home_search_bar_type_node
                    PlaceType.WAY -> R.drawable.ic_home_search_bar_type_way
                    PlaceType.RELATION -> R.drawable.ic_home_search_bar_type_relation
                },
                geoPoint = place.location.toGeoPoint(),
                boundingBox = place.boundingBox,
                isLandscape = false
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

    fun generatePlacesErrorItem(domainException: DomainException): List<SearchBarItem> {
        return listOf(
            SearchBarItem.Error(
                messageRes = domainException.messageRes,
                drawableRes = R.drawable.ic_search_bar_error
            )
        )
    }

    fun generatePlaceDetails(placeUiModel: PlaceUiModel): PlaceDetailsUiModel {
        return generatePlaceDetails(
            placeUiModel = placeUiModel,
            geometry = Geometry.Node(placeUiModel.osmId, placeUiModel.geoPoint.toLocation())
        )
    }

    fun generatePlaceDetails(placeUiModel: PlaceUiModel, geometry: Geometry): PlaceDetailsUiModel {
        return PlaceDetailsUiModel(
            placeUiModel = placeUiModel,
            geometryUiModel = when (geometry) {
                is Geometry.Node -> GeometryUiModel.Node(geometry.location.toGeoPoint())
                is Geometry.Way -> generateWayUiModel(geometry.osmId, geometry)
                is Geometry.Relation -> {
                    GeometryUiModel.Relation(
                        ways = geometry.ways.map { generateWayUiModel(geometry.osmId, it) }
                    )
                }
            }
        )
    }

    private fun generateWayUiModel(osmId: String, way: Geometry.Way): GeometryUiModel.Way {
        val locations = way.locations

        return GeometryUiModel.Way(
            osmId = osmId,
            geoPoints = locations.map { it.toGeoPoint() },
            isClosed = locations.first() == locations.last()
        )
    }

    fun generateLandscapes(landscapes: List<Landscape>): List<PlaceUiModel> {
        return landscapes.map { landscape ->
            PlaceUiModel(
                osmId = landscape.osmId,
                placeType = PlaceType.WAY,
                primaryText = landscape.name.toMessage(),
                secondaryText = R.string.home_bottom_sheet_landscape_secondary.toMessage(),
                iconRes = when (landscape.type) {
                    LandscapeType.MOUNTAIN_RANGE_LOW -> R.drawable.ic_landscapes_mountain_low
                    LandscapeType.MOUNTAIN_RANGE_HIGH -> R.drawable.ic_landscapes_mountain_high
                    LandscapeType.PLATEAU_WITH_WATER -> R.drawable.ic_landscapes_water
                    LandscapeType.CAVE_SYSTEM -> R.drawable.ic_landscapes_cave
                },
                geoPoint = landscape.center.toGeoPoint(),
                boundingBox = null,
                isLandscape = true
            )
        }
    }

    fun generateHikingRoutes(placeName: String, hikingRoutes: List<HikingRoute>): List<HikingRoutesItem> {
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

    fun generateHikingRouteDetails(hikingRoute: HikingRouteUiModel, geometry: Geometry): PlaceDetailsUiModel {
        val relation = geometry as? Geometry.Relation ?: error("Hiking route can only be a Relation")

        val totalDistance = relation.ways.sumOf { it.distance }

        return generatePlaceDetails(
            placeUiModel = PlaceUiModel(
                osmId = hikingRoute.osmId,
                primaryText = hikingRoute.name.toMessage(),
                secondaryText = DistanceFormatter.format(totalDistance),
                placeType = PlaceType.RELATION,
                iconRes = hikingRoute.symbolIcon,
                geoPoint = relation.ways.flatMap { it.locations }
                    .calculateCenter()
                    .toGeoPoint(),
                boundingBox = null,
                isLandscape = false
            ),
            geometry = geometry
        )
    }

}

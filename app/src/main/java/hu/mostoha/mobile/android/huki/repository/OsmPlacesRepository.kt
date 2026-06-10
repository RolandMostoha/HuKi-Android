package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.HikingRouteDetails
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.interactor.isRetriableOverpassError
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDetailsNetworkDomainMapper
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.huki.overpasser.output.OutputModificator
import hu.mostoha.mobile.android.huki.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.huki.overpasser.query.OverpassQuery
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class OsmPlacesRepository @Inject constructor(
    private val overpassService: OverpassService,
    private val placeDetailsNetworkDomainMapper: PlaceDetailsNetworkDomainMapper
) : PlacesRepository {

    companion object {
        const val OSM_HIKING_ROUTES_QUERY_LIMIT = 30
        const val OSM_PLACE_CATEGORY_QUERY_LIMIT = 100
        val TIMEOUT_S = OverpassService.OVERPASS_TIMEOUT_MS.milliseconds.inWholeSeconds.toInt()

        private const val OVERPASS_MAX_ATTEMPTS = 2
        private val OVERPASS_RETRY_DELAY = 700.milliseconds
    }

    override suspend fun getGeometry(osmId: String, placeType: PlaceType): Geometry {
        return when (placeType) {
            PlaceType.NODE -> {
                val response = getNode(osmId)

                placeDetailsNetworkDomainMapper.mapGeometryByNode(response, osmId)
            }
            PlaceType.WAY -> {
                val response = getNodesByWay(osmId)

                placeDetailsNetworkDomainMapper.mapGeometryByWay(response, osmId)
            }
            PlaceType.RELATION, PlaceType.HIKING_ROUTE -> {
                val response = getNodesByRelation(osmId)

                placeDetailsNetworkDomainMapper.mapGeometryByRelation(response, osmId)
            }
        }
    }

    override suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute> {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .rel()
            .tag("type", "route")
            .tag("route", "hiking")
            .tag("jel")
            .boundingBox(boundingBox.south, boundingBox.west, boundingBox.north, boundingBox.east)
            .end()
            .output(OutputVerbosity.TAGS, null, null, OSM_HIKING_ROUTES_QUERY_LIMIT)
            .build()

        val response = withOverpassRetry { overpassService.interpreter(query) }

        return placeDetailsNetworkDomainMapper.mapHikingRoutes(response)
    }

    override suspend fun getHikingRouteDetails(osmRelId: String): HikingRouteDetails {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .relBy(osmRelId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, 1)
            .build()

        val response = withOverpassRetry { overpassService.interpreter(query) }

        return HikingRouteDetails(
            hikingRoute = placeDetailsNetworkDomainMapper.mapHikingRoutes(response).first(),
            geometry = placeDetailsNetworkDomainMapper.mapGeometryByRelation(response, osmRelId)
        )
    }

    override suspend fun getPlacesByCategories(categories: Set<PlaceCategory>, boundingBox: BoundingBox): List<Place> {
        val osmQueryTags = categories.flatMap { it.osmQueryTags }
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .nwrs(osmQueryTags, boundingBox.south, boundingBox.west, boundingBox.north, boundingBox.east)
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.BB, null, OSM_PLACE_CATEGORY_QUERY_LIMIT)
            .build()

        val response = withOverpassRetry { overpassService.interpreter(query) }

        return placeDetailsNetworkDomainMapper.mapPlacesByCategories(response, categories)
    }

    override suspend fun getOsmTags(osmId: String, placeType: PlaceType): Map<String, String> {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .apply {
                when (placeType) {
                    PlaceType.NODE -> nodeBy(osmId)
                    PlaceType.WAY -> wayBy(osmId)
                    else -> relBy(osmId)
                }
            }
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.BB, null, 1)
            .build()

        return withOverpassRetry { overpassService.interpreter(query) }.elements.firstOrNull()?.tags ?: emptyMap()
    }

    private suspend fun getNode(osmId: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .nodeBy(osmId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, 1)
            .build()

        return withOverpassRetry { overpassService.interpreter(query) }
    }

    private suspend fun getNodesByWay(osmId: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .wayBy(osmId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return withOverpassRetry { overpassService.interpreter(query) }
    }

    private suspend fun getNodesByRelation(osmId: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_S)
            .filterQuery()
            .relBy(osmId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return withOverpassRetry { overpassService.interpreter(query) }
    }

    /**
     * Runs an Overpass [request] with a single retry on transient timeout/gateway errors.
     *
     * A cold query often exceeds the public instance's gateway timeout (HTTP 504) while it reads the
     * queried area from disk, but that first attempt warms the server's page cache, so an immediate
     * retry usually returns instantly. Only [isRetriableOverpassError] cases are retried; 429 and all
     * other errors propagate unchanged so the rate-limit is respected.
     */
    private suspend fun <T> withOverpassRetry(request: suspend () -> T): T {
        repeat(OVERPASS_MAX_ATTEMPTS - 1) {
            try {
                return request()
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                if (!exception.isRetriableOverpassError()) throw exception

                Timber.w(exception, "Overpass request failed, retrying in $OVERPASS_RETRY_DELAY")

                delay(OVERPASS_RETRY_DELAY)
            }
        }

        return request()
    }

}

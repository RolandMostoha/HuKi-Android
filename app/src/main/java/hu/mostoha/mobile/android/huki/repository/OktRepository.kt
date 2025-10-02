package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import androidx.core.util.toRange
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRouteGeometry
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.mapper.OktRoutesMapper
import hu.mostoha.mobile.android.huki.util.distanceBetween
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OktRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val oktRoutesMapper: OktRoutesMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getOktRoutes(oktType: OktType): OktRoutes {
        return withContext(ioDispatcher) {
            val inputStream = when (oktType) {
                OktType.OKT -> context.resources.openRawResource(R.raw.okt_teljes_bh_20250924)
                OktType.RPDDK -> context.resources.openRawResource(R.raw.rpddk_teljes_bh_20250909)
                OktType.AKT -> context.resources.openRawResource(R.raw.ak_teljes_bh_20250903)
            }

            val gpx = GPXParser().parse(inputStream)
            val gpxWaypoints = gpx.wayPoints
            val stampWaypoints = oktRoutesMapper.map(oktType, gpxWaypoints)
            check(gpxWaypoints.count() == stampWaypoints.map { it.stampTag }.count()) {
                "GPX waypoints count should match with stamp numbers count"
            }

            val allLocations = gpx.tracks
                .flatMap { it.trackSegments }
                .flatMap { it.trackPoints }
                .map { trackPoint ->
                    Location(trackPoint.latitude, trackPoint.longitude, trackPoint.elevation)
                }

            val routes = oktType.oktRouteList.map { oktRoute ->
                val isFullRoute = oktRoute.id == oktType.fullRouteId
                val routeLocations = if (isFullRoute) {
                    allLocations
                } else {
                    val closestStartToOktFull = allLocations.minBy { it.distanceBetween(oktRoute.start) }
                    val closestEndToOktFull = allLocations.minBy { it.distanceBetween(oktRoute.end) }
                    val fromIndex = allLocations.indexOf(closestStartToOktFull)
                    val toIndex = allLocations.indexOf(closestEndToOktFull)

                    allLocations.subList(fromIndex, toIndex + 1)
                }

                val stampWaypoints = if (isFullRoute) {
                    emptyList()
                } else {
                    stampWaypoints.filter { it.stampTag.stampNumber in oktRoute.stampTagsRange.toRange() }
                }

                OktRouteGeometry(oktRoute, routeLocations, stampWaypoints)
            }

            OktRoutes(allLocations, stampWaypoints, routes)
        }
    }

}

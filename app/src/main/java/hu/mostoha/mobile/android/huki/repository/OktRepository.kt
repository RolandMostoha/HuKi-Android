package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.OktStampWaypoint
import hu.mostoha.mobile.android.huki.model.mapper.OktRoutesMapper
import io.ticofab.androidgpxparser.parser.GPXParser
import javax.inject.Inject

class OktRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val oktRoutesMapper: OktRoutesMapper,
) {

    fun getOktRoutes(): OktRoutes {
        return OktRoutes(
            locations = getOktFullRoute(),
            stampWaypoints = getOktStampWaypoints(),
        )
    }

    private fun getOktFullRoute(): List<Location> {
        val inputStream = context.resources.openRawResource(R.raw.okt_full)

        val gpx = GPXParser().parse(inputStream)

        return gpx.tracks
            .flatMap { it.trackSegments }
            .flatMap { it.trackPoints }
            .map { trackPoint ->
                Location(trackPoint.latitude, trackPoint.longitude, trackPoint.elevation)
            }
    }

    private fun getOktStampWaypoints(): List<OktStampWaypoint> {
        val inputStream = context.resources.openRawResource(R.raw.okt_stamp_locations)

        val gpx = GPXParser().parse(inputStream)
        val gpxWaypoints = gpx.wayPoints

        val waypoints = oktRoutesMapper.map(gpxWaypoints)

        check(gpxWaypoints.count() == waypoints.map { it.stampNumber }.count()) {
            "GPX waypoints count should match with stamp numbers count"
        }

        return waypoints
    }

}

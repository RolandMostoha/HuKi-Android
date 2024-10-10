package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.mapper.OktRoutesMapper
import io.ticofab.androidgpxparser.parser.GPXParser
import javax.inject.Inject

class OktRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val oktRoutesMapper: OktRoutesMapper,
) {

    fun getOktRoutes(): OktRoutes {
        val inputStream = context.resources.openRawResource(R.raw.okt_teljes_bh_20240923)

        val gpx = GPXParser().parse(inputStream)

        val gpxWaypoints = gpx.wayPoints
        val stampWaypoints = oktRoutesMapper.map(gpxWaypoints)
        check(gpxWaypoints.count() == stampWaypoints.map { it.stampNumber }.count()) {
            "GPX waypoints count should match with stamp numbers count"
        }

        val locations = gpx.tracks
            .flatMap { it.trackSegments }
            .flatMap { it.trackPoints }
            .map { trackPoint ->
                Location(trackPoint.latitude, trackPoint.longitude, trackPoint.elevation)
            }

        return OktRoutes(locations, stampWaypoints)
    }

}

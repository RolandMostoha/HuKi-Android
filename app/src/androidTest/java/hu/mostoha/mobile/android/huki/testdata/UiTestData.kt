package hu.mostoha.mobile.android.huki.testdata

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import java.io.File
import kotlin.time.Duration.Companion.minutes

val DEFAULT_MY_LOCATION = Location(
    DEFAULT_MY_LOCATION_LATITUDE,
    DEFAULT_MY_LOCATION_LONGITUDE,
    DEFAULT_MY_LOCATION_ALTITUDE
)

object Landscapes {
    val DEFAULT_LANDSCAPE = LOCAL_LANDSCAPES.minBy { DEFAULT_MY_LOCATION.distanceBetween(it.center) }
    val DEFAULT_LANDSCAPE_2 = LOCAL_LANDSCAPES.sortedBy { DEFAULT_MY_LOCATION.distanceBetween(it.center) }[1]

    val DEFAULT_GEOMETRY_LANDSCAPE = Geometry.Way(
        osmId = DEFAULT_LANDSCAPE.osmId,
        locations = DEFAULT_LANDSCAPE_WAY_GEOMETRY.map { Location(it.first, it.second) },
        distance = 30_000
    )
}

object HikingRoutes {
    val DEFAULT_HIKING_ROUTE = HikingRoute(
        osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
        name = DEFAULT_HIKING_ROUTE_NAME,
        symbolType = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
    )
}

object Places {
    const val DEFAULT_SEARCH_TEXT = "Dobogoko"

    val DEFAULT_PLACE_NODE = Place(
        osmId = DEFAULT_NODE_OSM_ID,
        name = DEFAULT_NODE_NAME,
        placeType = PlaceType.NODE,
        location = Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE)
    )

    val DEFAULT_GEOMETRY_NODE = Geometry.Node(
        osmId = DEFAULT_PLACE_NODE.osmId,
        location = DEFAULT_PLACE_NODE.location
    )

    val DEFAULT_PLACE_WAY = Place(
        osmId = DEFAULT_WAY_OSM_ID,
        name = DEFAULT_WAY_NAME,
        placeType = PlaceType.WAY,
        location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE)
    )

    val DEFAULT_GEOMETRY_WAY = Geometry.Way(
        osmId = DEFAULT_PLACE_WAY.osmId,
        locations = DEFAULT_WAY_GEOMETRY.map { Location(it.first, it.second) },
        distance = 100
    )

    val DEFAULT_PLACE_RELATION = Place(
        osmId = DEFAULT_RELATION_OSM_ID,
        name = DEFAULT_RELATION_NAME,
        placeType = PlaceType.RELATION,
        location = Location(DEFAULT_RELATION_CENTER_LATITUDE, DEFAULT_RELATION_CENTER_LONGITUDE)
    )

    val DEFAULT_GEOMETRY_RELATION = Geometry.Relation(
        osmId = DEFAULT_PLACE_RELATION.osmId,
        ways = listOf(
            Geometry.Way(
                osmId = DEFAULT_RELATION_WAY_1_OSM_ID,
                locations = DEFAULT_RELATION_WAY_1_GEOMETRY.map { Location(it.first, it.second) },
                distance = 1500
            ),
            Geometry.Way(
                osmId = DEFAULT_RELATION_WAY_2_OSM_ID,
                locations = DEFAULT_RELATION_WAY_2_GEOMETRY.map { Location(it.first, it.second) },
                distance = 2000
            )
        )
    )
}

object Gpx {

    const val TEST_GPX_NAME = "dera_szurdok.gpx"

    fun getTestGpxFileResult(fileName: String = TEST_GPX_NAME): Instrumentation.ActivityResult {
        val inputStream = testContext.assets.open(fileName)
        val file = File(testAppContext.cacheDir.path + "/$fileName").apply {
            copyFrom(inputStream)
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            data = Uri.fromFile(file)
        }

        return Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
    }

    fun getTestGpxFileUri(): Uri {
        val inputStream = testContext.assets.open(TEST_GPX_NAME)
        val file = File(testAppContext.cacheDir.path + TEST_GPX_NAME).apply {
            copyFrom(inputStream)
        }

        return Uri.fromFile(file)
    }

}

object RoutePlanner {

    private val DEFAULT_WAYPOINTS = listOf(
        Location(
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
        ),
        Location(
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
        )
    )

    val DEFAULT_ROUTE_PLAN = RoutePlan(
        wayPoints = DEFAULT_WAYPOINTS,
        locations = DEFAULT_WAYPOINTS,
        travelTime = 100L.minutes,
        distance = 13000,
        altitudeRange = Pair(
            DEFAULT_WAYPOINTS.minOf { it.altitude!! }.toInt(),
            DEFAULT_WAYPOINTS.maxOf { it.altitude!! }.toInt()
        ),
        incline = 500,
        decline = 200,
        isClosed = false
    )

}

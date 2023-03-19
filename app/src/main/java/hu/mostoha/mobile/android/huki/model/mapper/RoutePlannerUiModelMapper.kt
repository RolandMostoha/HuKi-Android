package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.extensions.getRandomNumberText
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.AltitudeUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointItem
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import org.osmdroid.util.BoundingBox
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.Normalizer
import javax.inject.Inject
import kotlin.math.min


class RoutePlannerUiModelMapper @Inject constructor() {

    fun mapToRoutePlanUiModel(name: String, triggerLocations: List<Location>, routePlan: RoutePlan): RoutePlanUiModel {
        val geoPoints = routePlan.locations.map { it.toGeoPoint() }
        val altitudeRange = routePlan.altitudeRange
        val wayPoints = routePlan.wayPoints
        val wayPointItems = wayPoints.mapIndexedNotNull { index, location ->
            if (index == 0 && routePlan.isClosed) {
                return@mapIndexedNotNull null
            }

            WaypointItem(
                order = index,
                waypointType = when (index) {
                    0 -> WaypointType.START
                    wayPoints.lastIndex -> WaypointType.END
                    else -> WaypointType.INTERMEDIATE
                },
                location = location,
            )
        }

        return RoutePlanUiModel(
            id = routePlan.id,
            name = name,
            triggerLocations = triggerLocations,
            wayPoints = wayPointItems,
            geoPoints = geoPoints,
            boundingBox = BoundingBox.fromGeoPoints(geoPoints).toDomainBoundingBox(),
            travelTimeText = routePlan.travelTime.formatHoursAndMinutes().toMessage(),
            distanceText = DistanceFormatter.format(routePlan.distance),
            altitudeUiModel = AltitudeUiModel(
                minAltitudeText = DistanceFormatter.format(altitudeRange.first),
                maxAltitudeText = DistanceFormatter.format(altitudeRange.second),
                uphillText = DistanceFormatter.format(routePlan.incline),
                downhillText = DistanceFormatter.format(routePlan.decline),
            ),
            isClosed = routePlan.isClosed
        )
    }

    fun mapToRoutePlanName(waypointItems: List<WaypointItem>): String {
        val waypointsWithLocation = waypointItems.filter { it.location != null }

        check(waypointsWithLocation.size >= 2) {
            "At lease two waypoints are needed to create route plan name"
        }

        val firstWaypointName = getWaypointName(waypointsWithLocation.first())
        val lastWaypointName = getWaypointName(waypointsWithLocation.last { it.location != null })
        val id = getRandomNumberText(MAX_CHAR_ID)

        return "${firstWaypointName}_${lastWaypointName}_HuKi$id"
    }

    private fun getWaypointName(waypointItem: WaypointItem): String {
        return if (waypointItem.primaryText is Message.Text) {
            val primaryText = waypointItem.primaryText.text
            val substring = primaryText.substring(0, min(MAX_CHAR_WAYPOINT, primaryText.length))
            val normalized = Normalizer.normalize(substring, Normalizer.Form.NFD)
            val withoutAccent = REGEX_WITHOUT_ACCENT.replace(normalized, "")

            REGEX_SPECIAL.replace(withoutAccent, "_")
        } else {
            val location = waypointItem.location!!
            val formatted = LOCATION_DECIMAL_FORMAT.format(location.latitude)
                .plus(",")
                .plus(LOCATION_DECIMAL_FORMAT.format(location.longitude))

            REGEX_SPECIAL.replace(formatted, "_")
        }
    }

    companion object {
        private const val MAX_CHAR_ID = 3
        private const val MAX_CHAR_WAYPOINT = 10
        private val REGEX_WITHOUT_ACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        private val REGEX_SPECIAL = "[^A-Za-z0-9]".toRegex()
        private val LOCATION_DECIMAL_FORMAT = DecimalFormat("0.00").apply {
            roundingMode = RoundingMode.DOWN
        }
    }

}

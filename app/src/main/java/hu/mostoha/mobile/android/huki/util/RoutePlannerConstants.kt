package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.network.graphhopper.CustomModel
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Priority

const val ROUTE_PLANNER_MAX_WAYPOINT_COUNT = 12

/**
 * Credits left for iOS, not yet updated Android clients and routes abroad.
 */
const val GRAPHHOPPER_RESERVE_CREDITS = 500

val HUKI_ROUTING_BOUNDING_BOX = BoundingBox(
    north = 48.60,
    east = 22.94,
    south = 45.71,
    west = 16.09,
)

val HIKE_CUSTOM_MODEL = CustomModel(
    listOf(
        Priority(
            ifCondition = "foot_network == MISSING",
            multiplyBy = "0.3"
        )
    )
)

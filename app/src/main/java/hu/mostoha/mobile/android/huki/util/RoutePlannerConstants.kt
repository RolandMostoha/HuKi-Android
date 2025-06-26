package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.network.graphhopper.CustomModel
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Priority

const val ROUTE_PLANNER_MAX_WAYPOINT_COUNT = 12

val HIKE_CUSTOM_MODEL = CustomModel(
    listOf(
        Priority(
            ifCondition = "foot_network == MISSING",
            multiplyBy = "0.3"
        )
    )
)

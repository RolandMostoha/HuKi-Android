package hu.mostoha.mobile.android.huki.ui.home.routeplanner

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.WaypointComment
import java.util.UUID

data class WaypointItem(
    val id: Long = UUID.randomUUID().mostSignificantBits,
    val order: Int,
    val waypointType: WaypointType,
    val primaryText: Message? = null,
    val location: Location? = null,
    val searchText: String? = null,
    val waypointComment: WaypointComment? = null
)

fun WaypointItem.hasLocation(): Boolean {
    return this.location != null
}

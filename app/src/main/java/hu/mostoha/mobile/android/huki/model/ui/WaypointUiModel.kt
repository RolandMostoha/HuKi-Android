package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import org.osmdroid.util.GeoPoint

data class WaypointUiModel(
    val geoPoint: GeoPoint,
    val waypointType: WaypointType,
    val name: Message? = null,
)

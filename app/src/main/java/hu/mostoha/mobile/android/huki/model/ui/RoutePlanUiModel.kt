package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointItem
import org.osmdroid.util.GeoPoint

data class RoutePlanUiModel(
    val id: String,
    val name: String,
    val triggerLocations: List<Location>,
    val wayPoints: List<WaypointItem>,
    val geoPoints: List<GeoPoint>,
    val boundingBox: BoundingBox,
    val travelTimeText: Message.Text,
    val distanceText: Message.Res,
    val altitudeUiModel: AltitudeUiModel,
    val isClosed: Boolean,
    val isReturnToHomeAvailable: Boolean,
)

package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import org.osmdroid.util.GeoPoint

data class GpxDetailsUiModel(
    val id: String,
    val name: String,
    val geoPoints: List<GeoPoint>,
    val waypoints: List<WaypointUiModel>,
    val boundingBox: BoundingBox,
    val travelTimeText: Message.Text?,
    val distanceText: Message.Res?,
    val altitudeUiModel: AltitudeUiModel?,
    val isVisible: Boolean
)

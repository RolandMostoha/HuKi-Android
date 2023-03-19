package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import org.osmdroid.util.GeoPoint

data class GpxDetailsUiModel(
    val id: String,
    val name: String,
    val start: GeoPoint,
    val end: GeoPoint,
    val geoPoints: List<GeoPoint>,
    val boundingBox: BoundingBox,
    val travelTimeText: Message.Text,
    val distanceText: Message.Res,
    val altitudeUiModel: AltitudeUiModel?,
    val isClosed: Boolean,
    val isVisible: Boolean
)

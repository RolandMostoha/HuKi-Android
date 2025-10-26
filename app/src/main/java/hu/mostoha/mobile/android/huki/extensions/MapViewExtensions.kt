package hu.mostoha.mobile.android.huki.extensions

import android.graphics.drawable.Drawable
import android.widget.Toast
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.DistanceInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OVERLAY_TYPE_ORDER_MAP
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceDetailsMarker
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.util.MAP_RESET_ORIENTATION_ANIMATION_DURATION
import hu.mostoha.mobile.android.huki.util.distanceBetween
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

private const val MAP_ANIMATION_DURATION = 1000L

fun MapView.addOverlay(overlay: Overlay, comparator: Comparator<Overlay>) {
    overlays.add(overlay)
    overlays.sortWith(comparator)
    invalidate()
}

fun MapView.replaceOverlay(overlay: Overlay, comparator: Comparator<Overlay>) {
    overlays.removeIf { it.javaClass == overlay.javaClass }
    addOverlay(overlay, comparator)
}

fun MapView.hasOverlay(overlayId: String): Boolean {
    return overlays.any { it is OverlayWithIW && it.id == overlayId }
}

fun MapView.hasOverlay(overlayType: OverlayType): Boolean {
    val overlayClasses = OVERLAY_TYPE_ORDER_MAP.getValue(overlayType)

    return overlays.any { overlayClasses.contains(it::class) }
}

fun MapView.hasNoOverlay(overlayId: String): Boolean {
    return !hasOverlay(overlayId)
}

fun MapView.hasNoOverlay(overlayType: OverlayType): Boolean {
    return !hasOverlay(overlayType)
}

inline fun <reified T : OverlayWithIW> MapView.switchOverlayVisibility(overlayId: String) {
    overlays.filterIsInstance<T>()
        .filter { it.id == overlayId }
        .forEach { it.isEnabled = !it.isEnabled }
    invalidate()
}

fun MapView.updateOverlayVisibility(overlayId: String, isVisible: Boolean) {
    overlays.filterIsInstance<OverlayWithIW>()
        .filter { it.id == overlayId }
        .forEach { it.isEnabled = isVisible }
    invalidate()
}

inline fun <reified T : OverlayWithIW> MapView.updateOverlayVisibilityBy(overlayId: String, isVisible: Boolean) {
    overlays.filterIsInstance<T>()
        .filter { it.id == overlayId }
        .forEach { it.isEnabled = isVisible }
    invalidate()
}

fun MapView.updateOverlayVisibility(overlayType: OverlayType, isVisible: Boolean) {
    val overlayClasses = OVERLAY_TYPE_ORDER_MAP.getValue(overlayType)
    overlays
        .filter { overlayClasses.contains(it::class) }
        .forEach { it.isEnabled = isVisible }
    invalidate()
}

fun MapView.findMarker(geoPoint: GeoPoint): Marker? {
    return overlays
        .filterIsInstance<Marker>()
        .first { it.position == geoPoint }
}

inline fun <reified T : InfoWindow> MapView.openInfoWindows() {
    overlays.filterIsInstance<Marker>()
        .filter { it.infoWindow != null && it.infoWindow is T }
        .forEach { marker ->
            marker.showInfoWindow()
        }
}

inline fun <reified T : InfoWindow> MapView.areInfoWindowsClosed(): Boolean {
    return overlays.filterIsInstance<Marker>()
        .filter { it.infoWindow != null && it.infoWindow is T }
        .map { !it.isInfoWindowOpen }
        .all { it }
}

inline fun <reified T : InfoWindow> MapView.closeInfoWindows() {
    overlays.filterIsInstance<Marker>()
        .filter { it.infoWindow != null && it.infoWindow is T }
        .forEach { marker ->
            marker.infoWindow.close()
        }
}

inline fun <reified T : InfoWindow> MapView.toggleInfoWindows() {
    val isAnyInfoWindowOpen = overlays.filterIsInstance<Marker>()
        .filter { it.infoWindow != null && it.infoWindow is T }
        .any { it.isInfoWindowOpen }

    if (isAnyInfoWindowOpen) {
        closeInfoWindows<T>()
    } else {
        openInfoWindows<T>()
    }
}

inline fun <reified M : Marker> MapView.openAllInfoWindowsForMarkers() {
    overlays.filterIsInstance<M>()
        .filter { it.infoWindow != null }
        .forEach { marker ->
            marker.showInfoWindow()
        }
}

inline fun <reified M : Marker> MapView.closeAllInfoWindowsForMarkers() {
    overlays.filterIsInstance<M>()
        .filter { it.infoWindow != null }
        .forEach { marker ->
            marker.closeInfoWindow()
        }
}

inline fun <reified I : InfoWindow, reified M : Marker> MapView.openInfoWindowsForMarkers() {
    overlays.filterIsInstance<M>()
        .filter { it.infoWindow != null && it.infoWindow is I }
        .forEach { marker ->
            marker.showInfoWindow()
        }
}

inline fun <reified I : InfoWindow, reified M : Marker> MapView.closeInfoWindowsForMarkers() {
    overlays.filterIsInstance<M>()
        .filter { it.infoWindow != null && it.infoWindow is I }
        .forEach { marker ->
            marker.closeInfoWindow()
        }
}

inline fun <reified T : InfoWindow> MapView.doOnInfoWindows(block: (Marker, T) -> Unit) {
    overlays.filterIsInstance<Marker>()
        .filter { it.infoWindow != null && it.infoWindow is T }
        .forEach { marker ->
            block.invoke(marker, marker.infoWindow as T)
        }
}

fun MapView.removeOverlay(overlayId: String) {
    overlays.removeAll { it is OverlayWithIW && it.id == overlayId }
    invalidate()
}

fun MapView.removeOverlay(overlayType: OverlayType) {
    val overlayClasses = OVERLAY_TYPE_ORDER_MAP.getValue(overlayType)
    overlays.removeIf { overlayClasses.contains(it::class) }
    invalidate()
}

fun MapView.removeOverlays(overlayTypes: List<OverlayType>) {
    overlayTypes.forEach { overlayType ->
        val overlayClasses = OVERLAY_TYPE_ORDER_MAP.getValue(overlayType)
        overlays.removeIf { overlayClasses.contains(it::class) }
        invalidate()
    }
}

fun MapView.removeOverlays(clazz: Class<out Overlay>) {
    overlays.removeAll { it is OverlayWithIW && it::class.java == clazz }
    invalidate()
}

fun MapView.addMarker(
    overlayId: String = UUID.randomUUID().toString(),
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    onClick: (Marker) -> Unit
): Marker {
    val marker = Marker(this).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker)
            true
        }
    }

    addOverlay(marker, OverlayComparator)

    return marker
}

fun MapView.addPlaceDetailsMarker(
    overlayId: String = UUID.randomUUID().toString(),
    name: Message,
    geoPoint: GeoPoint,
    iconDrawable: Drawable,
    isHikeModeEnabled: Boolean,
    myLocation: GeoPoint?,
    onClick: (PlaceDetailsMarker) -> Unit,
): Marker {
    val marker = PlaceDetailsMarker(this, name).apply {
        id = overlayId
        position = geoPoint
        icon = iconDrawable
        setOnMarkerClickListener { marker, _ ->
            onClick.invoke(marker as PlaceDetailsMarker)
            true
        }
        infoWindow = DistanceInfoWindow(this@addPlaceDetailsMarker).apply {
            if (myLocation != null) {
                title = DistanceFormatter
                    .formatWithoutScale(myLocation.toLocation().distanceBetween(geoPoint.toLocation()))
                    .resolve(context)
            }
        }
        if (isHikeModeEnabled) {
            showInfoWindow()
        }
    }

    addOverlay(marker, OverlayComparator)

    return marker
}

fun MapView.removeMarker(marker: Marker) {
    marker.infoWindow?.close()
    marker.remove(this)
    invalidate()
}

fun MapView.center(geoPoint: GeoPoint) {
    controller.animateTo(geoPoint)
}

fun MapView.centerAndZoom(geoPoint: GeoPoint, zoomLevel: Double, animated: Boolean = false) {
    if (animated) {
        controller.animateTo(geoPoint, zoomLevel, MAP_ANIMATION_DURATION)
    } else {
        controller.setZoom(zoomLevel)
        controller.setCenter(geoPoint)
    }
}

fun MapView.animateCenterAndZoomIn(geoPoint: GeoPoint, zoomLevel: Double) {
    if (zoomLevel > this.zoomLevelDouble) {
        controller.animateTo(geoPoint, zoomLevel, MAP_ANIMATION_DURATION)
    } else {
        controller.animateTo(geoPoint, this.zoomLevelDouble, MAP_ANIMATION_DURATION)
    }
}

fun MapView.addMapMovedListener(onMapMoved: () -> Unit) {
    addMapListener(object : MapListener {
        override fun onScroll(event: ScrollEvent): Boolean {
            onMapMoved.invoke()
            return true
        }

        override fun onZoom(event: ZoomEvent): Boolean {
            onMapMoved.invoke()
            return true
        }
    })
}

fun MapView.addZoomListener(onZoom: (event: ZoomEvent) -> Unit) {
    addMapListener(object : MapListener {
        var lastZoom = 0.0

        override fun onScroll(event: ScrollEvent): Boolean {
            return false
        }

        override fun onZoom(event: ZoomEvent): Boolean {
            onZoom.invoke(event)

            val actualZoom = BigDecimal(event.zoomLevel).setScale(2, RoundingMode.HALF_UP).toDouble()

            if (actualZoom != lastZoom) {
                this@addZoomListener.context.showToast("$actualZoom".toMessage(), Toast.LENGTH_SHORT)

                lastZoom = actualZoom
            }

            return true
        }
    })
}

fun MapView.addLongClickHandlerOverlay(onLongClick: (GeoPoint) -> Unit) {
    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            return false
        }

        override fun longPressHelper(p: GeoPoint): Boolean {
            onLongClick.invoke(p)
            return true
        }
    })
    addOverlay(mapEventsOverlay, OverlayComparator)
}

fun MapView.resetOrientation() {
    if (mapOrientation != 0f) {
        controller.animateTo(
            mapCenter,
            zoomLevelDouble,
            MAP_RESET_ORIENTATION_ANIMATION_DURATION,
            0f
        )
    }
}

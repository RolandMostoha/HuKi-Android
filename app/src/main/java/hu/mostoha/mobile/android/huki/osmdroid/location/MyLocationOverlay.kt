package hu.mostoha.mobile.android.huki.osmdroid.location

import android.location.Location
import androidx.lifecycle.LifecycleCoroutineScope
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.centerAndZoom
import hu.mostoha.mobile.android.huki.extensions.postMainDelayed
import hu.mostoha.mobile.android.huki.extensions.toBitmap
import hu.mostoha.mobile.android.huki.util.calculateZoomLevel
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.equalsDelta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.IOrientationConsumer
import org.osmdroid.views.overlay.compass.IOrientationProvider
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * [MyLocationNewOverlay] implementation that supports follow location callbacks and coroutine based location
 * monitoring via [AsyncMyLocationProvider]. Also supports sensor based compass via [IOrientationProvider].
 */
class MyLocationOverlay(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val provider: AsyncMyLocationProvider,
    private val mapView: MapView
) : MyLocationOverlayBase(provider, mapView), IOrientationConsumer {

    companion object {
        /**
         * Horizontal/vertical anchor scale values for my location icons.
         */
        private val MY_LOCATION_ICON_ANCHOR = 0.5f to 0.5f
        private val MY_LOCATION_COMPASS_ANCHOR = 0.5f to 0.75f

        private const val COMPASS_CHANGE_THRESHOLD = 2f
        private val COMPASS_ORIENTATION_RANGE = 0.0f..360.0f

        private const val MAX_DISTANCE_TO_ANIMATE = 10_000

        private const val LIVE_COMPASS_ANIMATION_DURATION = 100L
        private const val NEW_LOCATION_ANIMATION_DURATION = 300L
        private const val ZOOM_ANIMATION_DELAY = 1000L
    }

    private var isLocationEnabled = false
    private var isCompassEnabled = false

    private var orientationProvider = InternalCompassOrientationProvider(mapView.context)

    private var lockedZoom: Double? = null
    var onFollowLocationDisabled: (() -> Unit)? = null
    var onFollowLocationFirstFix: (() -> Unit)? = null
    var onOrientationChanged: ((Float) -> Unit)? = null

    var isLiveCompassEnabled = false

    init {
        setPersonIcon(R.drawable.ic_marker_my_location.toBitmap(mapView.context))
        setDirectionIcon(R.drawable.ic_marker_my_location_compass.toBitmap(mapView.context))
        setPersonAnchor(MY_LOCATION_ICON_ANCHOR.first, MY_LOCATION_ICON_ANCHOR.second)
        setDirectionAnchor(MY_LOCATION_COMPASS_ANCHOR.first, MY_LOCATION_COMPASS_ANCHOR.second)
    }

    fun startLocationFlow(): Flow<Location> {
        val isLocationEnabled = provider.startLocationProvider(this)

        this.isLocationEnabled = isLocationEnabled

        if (isLocationEnabled) {
            isCompassEnabled = orientationProvider.startOrientationProvider(this)
        }

        return provider.getLocationFlow()
    }

    override fun enableFollowLocation() {
        mIsFollowing = true
        enableAutoStop = true

        if (isMyLocationEnabled) {
            lifecycleScope.launch {
                provider.getLastKnownLocationCoroutine()?.let { location ->
                    onFollowLocationFirstFix?.invoke()
                    setLocation(location)
                }
            }
        }
    }

    override fun setLocation(location: Location) {
        super.setLocation(location)

        val geoPoint = GeoPoint(location)
        val actualZoom = mapView.zoomLevelDouble
        val calculatedZoomLevel = location.calculateZoomLevel().toDouble()
        val zoom = if (actualZoom > calculatedZoomLevel) {
            actualZoom
        } else {
            calculatedZoomLevel
        }

        if (mIsFollowing && !mapView.isAnimating) {
            if (mapView.mapCenter.distanceBetween(geoPoint) <= MAX_DISTANCE_TO_ANIMATE) {
                mapView.controller.animateTo(geoPoint, zoom, NEW_LOCATION_ANIMATION_DURATION)
            } else {
                mapView.centerAndZoom(geoPoint, zoom)
            }
        } else {
            mapView.postInvalidate()
        }

        if (isCompassEnabled) {
            val lastKnownOrientation = orientationProvider.lastKnownOrientation

            if (lastKnownOrientation in COMPASS_ORIENTATION_RANGE) {
                location.bearing = lastKnownOrientation
            }
        }
    }

    override fun disableMyLocation() {
        super.disableMyLocation()

        orientationProvider.stopOrientationProvider()

        isLocationEnabled = false
        isCompassEnabled = false
        isLiveCompassEnabled = false
    }

    override fun disableFollowLocation() {
        super.disableFollowLocation()
        onFollowLocationDisabled?.invoke()
    }

    override fun isMyLocationEnabled(): Boolean = isLocationEnabled

    override fun onOrientationChanged(orientation: Float, source: IOrientationProvider) {
        if (orientation !in COMPASS_ORIENTATION_RANGE) {
            return
        }

        val lastLocation = lastFix

        if (lastLocation != null) {
            if (!lastLocation.bearing.equalsDelta(orientation, COMPASS_CHANGE_THRESHOLD)) {
                lastLocation.bearing = orientation

                setLocation(lastLocation)
            }

            if (isLiveCompassEnabled && mIsFollowing) {
                onOrientationChanged?.invoke(orientation)

                mapView.controller.animateTo(
                    GeoPoint(lastLocation),
                    lockedZoom ?: mapView.zoomLevelDouble,
                    LIVE_COMPASS_ANIMATION_DURATION,
                    -orientation
                )
            }
        }
    }

    fun lockZoom(zoom: Double) {
        lockedZoom = zoom

        postMainDelayed(ZOOM_ANIMATION_DELAY) {
            lockedZoom = null
        }
    }

}

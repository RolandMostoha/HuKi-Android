package hu.mostoha.mobile.android.huki.osmdroid.location

import android.location.Location
import androidx.lifecycle.LifecycleCoroutineScope
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoom
import hu.mostoha.mobile.android.huki.extensions.centerAndZoom
import hu.mostoha.mobile.android.huki.extensions.toBitmap
import hu.mostoha.mobile.android.huki.util.calculateZoomLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * [MyLocationNewOverlay] implementation that supports follow location callbacks and coroutine based location
 * monitoring via [AsyncMyLocationProvider].
 */
class MyLocationOverlay(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val provider: AsyncMyLocationProvider,
    private val mapView: MapView
) : MyLocationNewOverlay(provider, mapView) {

    companion object {
        /**
         * Horizontal/vertical anchor scale values for my location icons.
         */
        val MY_LOCATION_ICON_ANCHOR = 0.5f to 0.5f
        val MY_LOCATION_COMPASS_ANCHOR = 0.5f to 0.75f
    }

    private var isLocationEnabled = false

    var isAnimationEnabled: Boolean = false
    var onFollowLocationDisabled: (() -> Unit)? = null
    var onFollowLocationFirstFix: (() -> Unit)? = null

    init {
        setPersonIcon(R.drawable.ic_marker_my_location.toBitmap(mapView.context))
        setDirectionIcon(R.drawable.ic_marker_my_location_compass.toBitmap(mapView.context))
        setPersonAnchor(MY_LOCATION_ICON_ANCHOR.first, MY_LOCATION_ICON_ANCHOR.second)
        setDirectionAnchor(MY_LOCATION_COMPASS_ANCHOR.first, MY_LOCATION_COMPASS_ANCHOR.second)
    }

    fun myLocationFlow(): Flow<Location> {
        val isStarted = provider.startLocationProvider(this)

        isLocationEnabled = isStarted

        val locationFlow = provider.getLocationFlow()

        mapView.postInvalidate()

        return locationFlow
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

        mapView.postInvalidate()
    }

    override fun setLocation(location: Location) {
        val zoomLevel = location.calculateZoomLevel().toDouble()

        if (mIsFollowing && !mapView.isAnimating && mapView.zoomLevelDouble != zoomLevel) {
            val geoPoint = GeoPoint(location)

            if (isAnimationEnabled) {
                mapView.animateCenterAndZoom(geoPoint, zoomLevel)
            } else {
                mapView.centerAndZoom(geoPoint, zoomLevel)
            }
        }

        super.setLocation(location)
    }

    override fun disableMyLocation() {
        super.disableMyLocation()

        isLocationEnabled = false
    }

    override fun disableFollowLocation() {
        super.disableFollowLocation()

        onFollowLocationDisabled?.invoke()
    }

    override fun isMyLocationEnabled(): Boolean = isLocationEnabled

}

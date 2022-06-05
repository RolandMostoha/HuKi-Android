package hu.mostoha.mobile.android.huki.osmdroid

import android.graphics.Bitmap
import android.location.Location
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoom
import hu.mostoha.mobile.android.huki.extensions.toBitmap
import hu.mostoha.mobile.android.huki.util.calculateZoomLevel
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * [MyLocationNewOverlay] implementation that supports follow location callbacks and fixes
 * my location button misalignment on map. It also supports providing coroutine based location
 * monitoring via [AsyncMyLocationProvider].
 */
class MyLocationOverlay(
    private val provider: AsyncMyLocationProvider,
    private val mapView: MapView
) : MyLocationNewOverlay(provider, mapView) {

    companion object {
        /**
         * Anchor scale values for the my location icon. Expected values between 0 and 1.
         */
        const val MY_LOCATION_ANCHOR_HORIZONTAL = 0.5f
        const val MY_LOCATION_ANCHOR_VERTICAL = 0.5f
        const val MY_LOCATION_COMPASS_ANCHOR_HORIZONTAL = 0.5f
        const val MY_LOCATION_COMPASS_ANCHOR_VERTICAL = 0.75f
    }

    private var isLocationEnabled = false

    var onFollowLocationDisabled: (() -> Unit)? = null

    init {
        setDirectionArrow(
            R.drawable.ic_marker_my_location.toBitmap(mapView.context),
            R.drawable.ic_marker_my_location_compass.toBitmap(mapView.context)
        )
    }

    override fun setLocation(location: Location) {
        val zoomLevel = location.calculateZoomLevel().toDouble()

        if (mIsFollowing && mMapView.zoomLevelDouble != zoomLevel) {
            mapView.animateCenterAndZoom(GeoPoint(location), zoomLevel)
        }

        super.setLocation(location)
    }

    fun enableMyLocationFlow(): Flow<Location> {
        val isStarted = provider.startLocationProvider(this)

        isLocationEnabled = isStarted

        val locationFlow = provider.getLocationFlow()

        mapView.postInvalidate()

        return locationFlow
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

    // TODO Remove after OSMDroid 6.2.0 release. https://github.com/osmdroid/osmdroid/issues/1360
    override fun setDirectionArrow(personBitmap: Bitmap, directionArrowBitmap: Bitmap) {
        super.setDirectionArrow(personBitmap, directionArrowBitmap)

        if (mPersonHotspot != null) {
            setPersonHotspot(
                personBitmap.width * MY_LOCATION_ANCHOR_HORIZONTAL,
                personBitmap.height * MY_LOCATION_ANCHOR_VERTICAL
            )
        }

        mDirectionArrowCenterX = mDirectionArrowBitmap.width * MY_LOCATION_COMPASS_ANCHOR_HORIZONTAL
        mDirectionArrowCenterY = mDirectionArrowBitmap.height * MY_LOCATION_COMPASS_ANCHOR_VERTICAL
    }

}

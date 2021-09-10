package hu.mostoha.mobile.android.huki.osmdroid

import android.location.Location
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoom
import hu.mostoha.mobile.android.huki.util.MY_LOCATION_DEFAULT_ZOOM
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MyLocationOverlay(
    provider: IMyLocationProvider,
    private val mapView: MapView
) : MyLocationNewOverlay(provider, mapView) {

    private var onLocationChanged: ((Location) -> Unit)? = null

    var onFollowLocationDisabled: (() -> Unit)? = null

    override fun setLocation(location: Location) {
        if (mIsFollowing && mMapView.zoomLevelDouble != MY_LOCATION_DEFAULT_ZOOM) {
            mapView.animateCenterAndZoom(GeoPoint(location), MY_LOCATION_DEFAULT_ZOOM)
        }

        super.setLocation(location)
    }

    override fun onLocationChanged(location: Location, source: IMyLocationProvider) {
        super.onLocationChanged(location, source)

        onLocationChanged?.invoke(location)
    }

    override fun disableFollowLocation() {
        super.disableFollowLocation()

        onFollowLocationDisabled?.invoke()
    }

}
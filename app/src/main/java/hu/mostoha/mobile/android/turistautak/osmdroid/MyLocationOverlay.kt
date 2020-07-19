package hu.mostoha.mobile.android.turistautak.osmdroid

import android.location.Location
import android.view.MotionEvent
import hu.mostoha.mobile.android.turistautak.constants.DEFAULT_ZOOM_FOR_MY_LOCATION
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MyLocationOverlay(
    provider: IMyLocationProvider,
    mapView: MapView
) : MyLocationNewOverlay(provider, mapView) {

    var onFollowLocationDisabled: (() -> Unit)? = null

    override fun setLocation(location: Location) {
        if (mIsFollowing && mMapView.zoomLevelDouble != DEFAULT_ZOOM_FOR_MY_LOCATION) {
            mMapView.controller.zoomTo(DEFAULT_ZOOM_FOR_MY_LOCATION)
            mMapView.postInvalidate()
        }

        super.setLocation(location)
    }

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && enableAutoStop) {
            onFollowLocationDisabled?.invoke()
        }
        return super.onTouchEvent(event, mapView)

    }

}
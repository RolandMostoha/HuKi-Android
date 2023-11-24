package hu.mostoha.mobile.android.huki.osmdroid.infowindow

import android.view.MotionEvent
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.views.StrokedTextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class DistanceInfoWindow(
    mapView: MapView,
) : InfoWindow(R.layout.info_window_distance, mapView) {

    var title: String? = null
        set(value) {
            field = value
            mView.findViewById<StrokedTextView>(R.id.distanceInfoWindowTitle).text = value
        }

    init {
        mView.setOnTouchListener { view, event ->
            view.performClick()
            if (event.action == MotionEvent.ACTION_UP) {
                close()
            }
            true
        }
    }

    override fun onOpen(item: Any?) {
        val titleTextView = mView.findViewById<StrokedTextView>(R.id.distanceInfoWindowTitle)

        titleTextView.text = title
    }

    override fun onClose() {
        // no-op
    }

}

package hu.mostoha.mobile.android.huki.osmdroid.infowindow

import android.view.MotionEvent
import android.widget.TextView
import hu.mostoha.mobile.android.huki.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MapInfoWindow(mapView: MapView, private val title: String) : InfoWindow(R.layout.map_info_window, mapView) {

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
        val titleTextView = mView.findViewById<TextView>(R.id.mapInfoWindowTitle)
        titleTextView.text = title
    }

    override fun onClose() {
        // no-op
    }

}

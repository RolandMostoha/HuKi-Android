package hu.mostoha.mobile.android.huki.osmdroid.infowindow

import android.view.MotionEvent
import android.widget.TextView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.gone
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class GpxMarkerInfoWindow(
    mapView: MapView,
    private val title: String,
    private val description: String? = null,
    private val onNavigationClick: () -> Unit,
) : InfoWindow(R.layout.info_window_gpx_marker, mapView) {

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
        val descriptionTextView = mView.findViewById<TextView>(R.id.mapInfoWindowDescription)
        val navigationButton = mView.findViewById<TextView>(R.id.mapInfoWindowNavigationButton)

        titleTextView.text = title

        if (description.isNullOrEmpty()) {
            descriptionTextView.gone()
        } else {
            descriptionTextView.text = description
        }

        navigationButton.setOnClickListener {
            onNavigationClick()
            close()
        }
    }

    override fun onClose() {
        // no-op
    }

}

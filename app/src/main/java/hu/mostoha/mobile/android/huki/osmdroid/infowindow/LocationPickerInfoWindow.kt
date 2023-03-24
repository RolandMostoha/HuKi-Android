package hu.mostoha.mobile.android.huki.osmdroid.infowindow

import android.view.MotionEvent
import android.widget.TextView
import hu.mostoha.mobile.android.huki.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class LocationPickerInfoWindow(
    mapView: MapView,
    private val onSaveClick: () -> Unit,
    private val onCloseClick: () -> Unit,
) : InfoWindow(R.layout.info_window_location_picker, mapView) {

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
        mView.findViewById<TextView>(R.id.locationPickerDoneButton).setOnClickListener {
            onSaveClick.invoke()
        }
        mView.findViewById<TextView>(R.id.locationPickerCloseButton).setOnClickListener {
            onCloseClick.invoke()
        }
    }

    override fun onClose() {
        // no-op
    }

}

package hu.mostoha.mobile.android.huki.osmdroid.infowindow

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.Message
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class NavigationMarkerInfoWindow(
    mapView: MapView,
    private val title: String,
    private val description: String? = null,
    private val onNavigationClick: (() -> Unit)? = null,
    private val onOpened: (() -> Unit)? = null,
    private val onClosed: (() -> Unit)? = null,
) : InfoWindow(R.layout.info_window_gpx_marker, mapView) {

    private var isClosing = false

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

        if (onNavigationClick != null) {
            navigationButton.visible()
            navigationButton.setOnClickListener {
                onNavigationClick.invoke()
                close()
            }
        } else {
            navigationButton.gone()
        }

        mView.alpha = 0f
        mView.animate()
            .alpha(1f)
            .setDuration(FADE_DURATION)
            .start()

        onOpened?.invoke()
    }

    fun showDistance(distanceText: Message?, travelTimeText: Message?) {
        mView.findViewById<View>(R.id.mapInfoWindowDistanceContainer).visibleOrGone(distanceText != null)
        mView.findViewById<TextView>(R.id.mapInfoWindowDistance).setMessageOrGone(distanceText)
        mView.findViewById<TextView>(R.id.mapInfoWindowTravelTime).setMessageOrGone(travelTimeText)
    }

    override fun open(`object`: Any?, position: GeoPoint?, offsetX: Int, offsetY: Int) {
        // osmdroid re-adds the view on open, so a pending fade-out must finish first
        closeImmediately()
        super.open(`object`, position, offsetX, offsetY)
    }

    override fun close() {
        if (!isOpen || isClosing) return

        isClosing = true
        mView.animate()
            .alpha(0f)
            .setDuration(FADE_DURATION)
            .withEndAction { closeImmediately() }
            .start()
    }

    override fun onDetach() {
        closeImmediately()
        super.onDetach()
    }

    override fun onClose() {
        onClosed?.invoke()
    }

    private fun closeImmediately() {
        mView?.animate()?.cancel()
        isClosing = false
        super.close()
    }

    companion object {
        private const val FADE_DURATION = 150L
    }

}

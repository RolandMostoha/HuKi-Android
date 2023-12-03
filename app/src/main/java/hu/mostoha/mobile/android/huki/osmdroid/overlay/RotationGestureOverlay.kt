package hu.mostoha.mobile.android.huki.osmdroid.overlay

import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.gestures.RotationGestureDetector
import kotlin.math.absoluteValue

class RotationGestureOverlay(
    private val mapView: MapView
) : Overlay(), RotationGestureDetector.RotationListener {

    companion object {
        /**
         * Rotation threshold to avoid triggering on small changes during zoom events.
         */
        private const val ROTATION_THRESHOLD = 0.8f
        private const val ROTATION_TRIGGER_DELTA_TIME = 25L
    }

    private val mRotationDetector: RotationGestureDetector = RotationGestureDetector(this)

    private var timeLastSet = 0L
    private var currentAngle = 0f

    lateinit var mapRotationListener: (Float) -> Unit

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        mRotationDetector.onTouch(event)

        return super.onTouchEvent(event, mapView)
    }

    override fun onRotate(deltaAngle: Float) {
        if (deltaAngle.absoluteValue < ROTATION_THRESHOLD) return

        currentAngle += deltaAngle

        if (System.currentTimeMillis() - ROTATION_TRIGGER_DELTA_TIME > timeLastSet) {
            timeLastSet = System.currentTimeMillis()

            val mapOrientation = mapView.mapOrientation + currentAngle

            mapView.mapOrientation = mapOrientation

            mapRotationListener.invoke(mapOrientation)
        }
    }

    override fun setEnabled(pEnabled: Boolean) {
        mRotationDetector.isEnabled = pEnabled

        super.setEnabled(pEnabled)
    }

}

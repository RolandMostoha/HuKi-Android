package hu.mostoha.mobile.android.huki.osmdroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay

class OsmCopyrightOverlay(
    private val context: Context,
    private val analyticsService: FirebaseAnalyticsService
) : Overlay() {

    private var textPaint: Paint = TextPaint().apply {
        isAntiAlias = true
        textSize = context.resources.getDimensionPixelSize(R.dimen.text_size_extra_small).toFloat()
        color = ContextCompat.getColor(context, R.color.colorCopyright)
        textAlign = Paint.Align.LEFT
        typeface = context.resources.getFont(R.font.opensans_semibold)
    }

    private val copyrightNotice = context.getString(R.string.licences_osm_copyright_notice)

    private val xOffset = context.resources.getDimensionPixelSize(R.dimen.space_medium)
    private val yOffset = context.resources.getDimensionPixelSize(R.dimen.home_copyright_bottom_margin)

    private val hitRect: Rect = Rect()
    private val textBounds: Rect = Rect()

    override fun draw(canvas: Canvas, projection: Projection) {
        val canvasHeight = canvas.height

        val x = xOffset.toFloat()
        val y = (canvasHeight - yOffset).toFloat()

        projection.save(canvas, false, false)

        canvas.drawText(copyrightNotice, x, y, textPaint)
        textPaint.getTextBounds(copyrightNotice, 0, copyrightNotice.length, textBounds)
        hitRect.set(
            0,
            canvasHeight - (textBounds.height() + 2 * yOffset),
            textBounds.width() + 2 * xOffset,
            canvasHeight
        )

        projection.restore(canvas, false)
    }

    override fun onSingleTapConfirmed(motionEvent: MotionEvent, mapView: MapView): Boolean {
        return if (isCopyrightHit(motionEvent, mapView)) {
            analyticsService.copyrightClicked()

            showLicencesDialog()

            true
        } else {
            super.onSingleTapConfirmed(motionEvent, mapView)
        }
    }

    private fun isCopyrightHit(event: MotionEvent, mapView: MapView): Boolean {
        val projection = mapView.projection ?: return false
        val screenRect = projection.intrinsicScreenRect

        val x = screenRect.left + event.x.toInt()
        val y = screenRect.top + event.y.toInt()

        return hitRect.contains(x, y)
    }

    private fun showLicencesDialog() {
        LicenserDialog(context)
            .setTitle(R.string.licences_title)
            .setCustomNoticeTitle(R.string.licences_notices)
            .setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_openstreetmap_title),
                    url = "https://www.openstreetmap.org/copyright",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_hiking_layer_title),
                    url = "https://data2.openstreetmap.hu/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_photon_title),
                    url = "https://photon.komoot.io/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setPositiveButton(R.string.licences_ok_button_title) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}

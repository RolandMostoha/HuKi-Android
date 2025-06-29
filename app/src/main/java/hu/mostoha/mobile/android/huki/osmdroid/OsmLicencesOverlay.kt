package hu.mostoha.mobile.android.huki.osmdroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay

class OsmLicencesOverlay(
    private val context: Context,
    private val analyticsService: AnalyticsService,
    insets: Insets,
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
    private val yOffset = context.resources.getDimensionPixelSize(R.dimen.home_copyright_bottom_margin) + insets.bottom

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

    @Suppress("LongMethod")
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
                    title = context.getString(R.string.licences_graphhopper),
                    url = context.getString(R.string.route_planner_graphhopper_url),
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
                    title = context.getString(R.string.licences_location_iq_title),
                    url = "https://locationiq.com/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_osmdroid),
                    url = "https://github.com/osmdroid/osmdroid",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_okt),
                    url = "https://www.kektura.hu/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_termeszetjaro),
                    url = "https://www.termeszetjaro.hu/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_kirandulastippek),
                    url = "https://kirandulastippek.hu/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_tuhu),
                    url = "https://turistautak.hu/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_merretekerjek),
                    url = "https://merretekerjek.hu/",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setLibrary(
                Library(
                    title = context.getString(R.string.licences_google_satellite),
                    url = "https://developers.google.com/maps/documentation/tile/satellite",
                    license = License.CREATIVE_COMMONS
                )
            )
            .setPositiveButton(R.string.licences_ok_button_title) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}

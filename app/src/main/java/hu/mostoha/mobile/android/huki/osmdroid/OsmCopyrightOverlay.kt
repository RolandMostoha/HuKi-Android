package hu.mostoha.mobile.android.huki.osmdroid

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import hu.mostoha.mobile.android.huki.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay

class OsmCopyrightOverlay(val context: Context) : CopyrightOverlay(context) {

    init {
        setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText))
        setOffset(
            context.resources.getDimensionPixelSize(R.dimen.space_medium),
            context.resources.getDimensionPixelSize(R.dimen.home_copyright_bottom_margin)
        )
        setCopyrightNotice(context.getString(R.string.licences_osm_copyright_notice))
    }

    override fun draw(canvas: Canvas, map: MapView, shadow: Boolean) {
        draw(canvas, map.projection)
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        showLicencesDialog()

        return true
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

package hu.mostoha.mobile.android.huki.model.generator.formatter

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import javax.inject.Inject

class DistanceFormatter @Inject constructor(@ApplicationContext val context: Context) {

    @Suppress("MagicNumber")
    fun format(meters: Int): String {
        val km = meters.toDouble() / 1000

        return if (km > 1) {
            context.getString(R.string.default_distance_template_km, km)
        } else {
            context.getString(R.string.default_distance_template_m, meters)
        }
    }

}

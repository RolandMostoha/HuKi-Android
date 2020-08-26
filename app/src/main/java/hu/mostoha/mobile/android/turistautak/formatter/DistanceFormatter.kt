package hu.mostoha.mobile.android.turistautak.formatter

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.R
import javax.inject.Inject

class DistanceFormatter @Inject constructor(@ApplicationContext val context: Context) {

    fun format(meters: Int): String {
        val km = meters.toDouble() / 1000
        return if (km > 1) {
            context.getString(R.string.default_distance_template_km, km)
        } else {
            context.getString(R.string.default_distance_template_m, meters)
        }
    }

}
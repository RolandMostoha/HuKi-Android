package hu.mostoha.mobile.android.huki.ui.util

import hu.mostoha.mobile.android.huki.R

object DistanceFormatter {

    @Suppress("MagicNumber")
    fun format(meters: Int): Message {
        val km = meters.toDouble() / 1000

        return if (km > 1) {
            Message.Res(R.string.default_distance_template_km, listOf(km))
        } else {
            Message.Res(R.string.default_distance_template_m, listOf(meters))
        }
    }

}

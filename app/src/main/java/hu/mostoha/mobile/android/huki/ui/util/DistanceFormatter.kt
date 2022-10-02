package hu.mostoha.mobile.android.huki.ui.util

import hu.mostoha.mobile.android.huki.R
import java.math.BigDecimal
import java.math.RoundingMode

object DistanceFormatter {

    @Suppress("MagicNumber")
    fun format(meters: Int): Message {
        val km = (meters.toDouble() / 1000)
            .toBigDecimal()
            .setScale(1, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        return if (km >= BigDecimal.ONE) {
            Message.Res(R.string.default_distance_template_km, listOf(km.toPlainString()))
        } else {
            Message.Res(R.string.default_distance_template_m, listOf(meters))
        }
    }

}

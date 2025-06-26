package hu.mostoha.mobile.android.huki.ui.formatter

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import java.math.BigDecimal
import java.math.RoundingMode

@Suppress("MagicNumber")
object DistanceFormatter {

    fun format(meters: Int): Message.Res {
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

    fun formatWithoutScale(meters: Int): Message.Res {
        val km = (meters.toDouble() / 1000)
            .toBigDecimal()
            .setScale(0, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        return if (km >= BigDecimal.ONE) {
            Message.Res(R.string.default_distance_template_km, listOf(km.toPlainString()))
        } else {
            Message.Res(R.string.default_distance_template_m, listOf(meters))
        }
    }

    fun formatKm(km: Int): Message.Res = format(km * 1000)

    fun formatSigned(meters: Int): Message.Res {
        val km = (meters.toDouble() / 1000)
            .toBigDecimal()
            .setScale(1, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val prefix = if (meters > 0) {
            "+"
        } else {
            ""
        }

        return if (km.abs() >= BigDecimal.ONE) {
            Message.Res(R.string.default_distance_template_km, listOf(prefix + km.toPlainString()))
        } else {
            Message.Res(R.string.default_distance_template_m, listOf(prefix + meters.toString()))
        }
    }

    fun Int.toMetersFromKm(): Int {
        return this * 1000
    }

}

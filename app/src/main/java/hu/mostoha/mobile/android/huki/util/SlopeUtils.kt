package hu.mostoha.mobile.android.huki.util

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage

fun calculateSlope(location1: Location, location2: Location): Double {
    val horizontalDistance = location1.distanceBetween(location2).toDouble()
    val location1Altitude = location1.altitude
    val location2Altitude = location2.altitude

    if (horizontalDistance == 0.0 || location1Altitude == null || location2Altitude == null) {
        return 0.0
    }

    val verticalChange = location2Altitude - location1Altitude
    val slopePercentage = (verticalChange / horizontalDistance) * PERCENTAGE_SCALAR

    return slopePercentage
}

fun getSlopeGradientDrawable(context: Context): GradientDrawable {
    return GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(
            ContextCompat.getColor(context, R.color.colorSlopeNegativeHigh),
            ContextCompat.getColor(context, R.color.colorSlopeNegativeMid),
            ContextCompat.getColor(context, R.color.colorSlopeZero),
            ContextCompat.getColor(context, R.color.colorSlopePositiveMid),
            ContextCompat.getColor(context, R.color.colorSlopePositiveHigh),
        )
    ).apply {
        gradientType = GradientDrawable.LINEAR_GRADIENT
        cornerRadius = context.resources.getDimension(R.dimen.default_corner_size_surface)
    }
}

val SLOPE_ROWS = listOf(
    SlopeExplanationRow(
        title = R.string.gpx_slope_explanation_negative_template.toMessage(listOf(SLOPE_PERCENTAGE_HIGH)),
        description = R.string.gpx_slope_explanation_negative_high_desc.toMessage(),
        color = R.color.colorSlopeNegativeHigh,
    ),
    SlopeExplanationRow(
        title = R.string.gpx_slope_explanation_negative_template.toMessage(listOf(SLOPE_PERCENTAGE_MID)),
        description = R.string.gpx_slope_explanation_negative_mid_desc.toMessage(),
        color = R.color.colorSlopeNegativeMid,
    ),
    SlopeExplanationRow(
        title = R.string.gpx_slope_explanation_zero.toMessage(listOf(SLOPE_PERCENTAGE_MID)),
        description = R.string.gpx_slope_explanation_zero_desc.toMessage(),
        color = R.color.colorSlopeZero,
    ),
    SlopeExplanationRow(
        title = R.string.gpx_slope_explanation_positive_template.toMessage(listOf(SLOPE_PERCENTAGE_MID)),
        description = R.string.gpx_slope_explanation_positive_mid_desc.toMessage(),
        color = R.color.colorSlopePositiveMid,
    ),
    SlopeExplanationRow(
        title = R.string.gpx_slope_explanation_positive_template.toMessage(listOf(SLOPE_PERCENTAGE_HIGH)),
        description = R.string.gpx_slope_explanation_positive_high_desc.toMessage(),
        color = R.color.colorSlopePositiveHigh,
    ),
)

data class SlopeExplanationRow(
    val title: Message.Res,
    val description: Message.Res,
    @ColorRes val color: Int,
)

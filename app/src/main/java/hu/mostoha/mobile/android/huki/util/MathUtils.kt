package hu.mostoha.mobile.android.huki.util

import kotlin.math.roundToInt

/**
 * Maps the given [value] to 0.0..1.0 for the specified [min], [max] and [offset].
 *
 * @param min The minimum value
 * @param max The maximum value
 * @param offset The offset between [0.0..1.0]
 * @param value The value to map
 * @return The mapped value between [0.0..1.0]
 */
fun linearInterpolation(min: Float, max: Float, offset: Float, value: Float): Float {
    return offset + ((1 - offset) / (max - min) * (value - min))
}

const val PERCENTAGE_SCALAR = 100

fun Double.toPercentageFromScale(): Int {
    return (this * PERCENTAGE_SCALAR).roundToInt()
}

fun Int.toScaleFromPercentage(): Double {
    return this.toDouble() / PERCENTAGE_SCALAR
}

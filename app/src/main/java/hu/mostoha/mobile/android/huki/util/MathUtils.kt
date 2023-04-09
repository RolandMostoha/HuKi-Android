package hu.mostoha.mobile.android.huki.util

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

fun Float.toPercentageFromScale(): Int {
    return (this * PERCENTAGE_SCALAR).toInt()
}

fun Int.toScaleFromPercentage(): Float {
    return this.toFloat() / PERCENTAGE_SCALAR
}

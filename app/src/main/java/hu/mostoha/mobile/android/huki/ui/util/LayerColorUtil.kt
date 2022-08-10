package hu.mostoha.mobile.android.huki.ui.util

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.annotation.ColorInt

const val DARK_MODE_HIKING_LAYER_BRIGHTNESS = 0.85f

/**
 * Returns a [ColorMatrixColorFilter] which modifies the brightness.
 *
 * @param brightness the brightness factor from 0..1
 * @return the result [ColorMatrixColorFilter]
 */
fun getBrightnessColorMatrix(brightness: Float): ColorMatrixColorFilter {
    return ColorMatrixColorFilter(
        floatArrayOf(
            brightness, 0f, 0f, 0f, 0f,
            0f, brightness, 0f, 0f, 0f,
            0f, 0f, brightness, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
}

/**
 * Returns a [ColorMatrixColorFilter], which scales by a destination color, in a way similar to grayscale images.
 *
 * https://stackoverflow.com/a/53988096
 * https://medium.com/square-corner-blog/welcome-to-the-color-matrix-64d112e3f43d
 *
 * @param destinationColor the destination color to scale by
 * @return the result [ColorMatrixColorFilter]
 */
@Suppress("MagicNumber")
fun getColorScaledMatrix(@ColorInt destinationColor: Int): ColorMatrixColorFilter {
    val inverseMatrix = ColorMatrix(
        floatArrayOf(
            -1.0f, 0.0f, 0.0f, 0.0f, 255f,
            0.0f, -1.0f, 0.0f, 0.0f, 255f,
            0.0f, 0.0f, -1.0f, 0.0f, 255f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        )
    )

    val lr = (255.0f - Color.red(destinationColor)) / 255.0f
    val lg = (255.0f - Color.green(destinationColor)) / 255.0f
    val lb = (255.0f - Color.blue(destinationColor)) / 255.0f
    val grayscaleMatrix = ColorMatrix(
        floatArrayOf(
            lr, lg, lb, 0f, 0f,
            lr, lg, lb, 0f, 0f,
            lr, lg, lb, 0f, 0f,
            0f, 0f, 0f, 0f, 255f
        )
    )
    grayscaleMatrix.preConcat(inverseMatrix)

    val dr = Color.red(destinationColor)
    val dg = Color.green(destinationColor)
    val db = Color.blue(destinationColor)
    val drf = dr / 255f
    val dgf = dg / 255f
    val dbf = db / 255f
    val tintMatrix = ColorMatrix(
        floatArrayOf(
            drf, 0f, 0f, 0f, 0f,
            0f, dgf, 0f, 0f, 0f,
            0f, 0f, dbf, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    tintMatrix.preConcat(grayscaleMatrix)

    val lDestination = drf * lr + dgf * lg + dbf * lb
    val scale = 2f - lDestination
    val translate = 1 - scale * 0.5f
    val scaleMatrix = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, dr * translate,
            0f, scale, 0f, 0f, dg * translate,
            0f, 0f, scale, 0f, db * translate,
            0f, 0f, 0f, 1f, 0f
        )
    )
    scaleMatrix.preConcat(tintMatrix)

    return ColorMatrixColorFilter(scaleMatrix)
}

package hu.mostoha.mobile.android.huki.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import hu.mostoha.mobile.android.huki.extensions.isDarkMode

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

/**
 * Generates gradient colors for the given [scalars].
 *
 * @param startColor The start color for the minimum scalar value.
 * @param endColor The end color for the maximum scalar value.
 * @param scalars The scalar values.
 * @return The color list by scalar values.
 */
fun getGradientColors(
    @ColorInt startColor: Int,
    @ColorInt endColor: Int,
    scalars: List<Float>
): List<Int> {
    val min = scalars.min()
    val max = scalars.max()

    return scalars.map { scalar ->
        val weight = linearInterpolation(min, max, 0f, scalar)

        ColorUtils.blendARGB(startColor, endColor, weight)
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.productIconColor(context: Context): Int {
    return if (context.isDarkMode()) {
        this.lighten(0.65f).adjustBrightness(1.2f)
    } else {
        this
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.productTextColor(context: Context): Int {
    return if (context.isDarkMode()) {
        this.lighten(0.9f)
    } else {
        this.darken(0.35f)
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.productStrongTextColor(context: Context): Int {
    return if (context.isDarkMode()) {
        this.lighten(0.95f)
    } else {
        this.darken(0.45f)
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.productStrokeColor(context: Context): Int {
    return if (context.isDarkMode()) {
        this.darken(0.5f)
    } else {
        this.lighten(0.8f)
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.productBackgroundColor(context: Context): Int {
    return if (context.isDarkMode()) {
        this
    } else {
        this.adjustBrightness(1.5f).lighten(0.85f)
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.productHighlightColor(context: Context): Int {
    return if (context.isDarkMode()) {
        this.productIconColor(context).darken(0.2f)
    } else {
        this.lighten(0.2f)
    }
}

@Suppress("MagicNumber")
@ColorInt
fun Int.adjustBrightness(factor: Float): Int {
    val red = (Color.red(this) * factor).coerceIn(0f, 255f).toInt()
    val green = (Color.green(this) * factor).coerceIn(0f, 255f).toInt()
    val blue = (Color.blue(this) * factor).coerceIn(0f, 255f).toInt()
    return Color.rgb(red, green, blue)
}

fun @receiver:ColorInt Int.colorStateList(): ColorStateList {
    return ColorStateList.valueOf(this)
}

fun @receiver:ColorRes Int.colorStateList(context: Context): ColorStateList {
    return ContextCompat.getColorStateList(context, this)!!
}

@ColorInt
fun Context.color(@ColorRes res: Int) = ContextCompat.getColor(this, res)

@ColorInt
fun @receiver:ColorRes Int.color(context: Context) = context.color(this)

@ColorInt
fun @receiver:ColorInt Int.darken(factor: Float): Int {
    return ColorUtils.blendARGB(this, Color.BLACK, factor)
}

@ColorInt
fun @receiver:ColorInt Int.lighten(factor: Float): Int {
    return ColorUtils.blendARGB(this, Color.WHITE, factor)
}

fun @receiver:ColorRes Int.toColorFilter(context: Context): ColorFilter {
    return PorterDuffColorFilter(this.color(context), PorterDuff.Mode.SRC_IN)
}

fun @receiver:ColorInt Int.toColorFilter(): ColorFilter {
    return PorterDuffColorFilter(this, PorterDuff.Mode.SRC_IN)
}

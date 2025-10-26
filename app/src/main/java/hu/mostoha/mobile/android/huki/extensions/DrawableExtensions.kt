package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

fun @receiver:DrawableRes Int.toBitmap(context: Context): Bitmap {
    return ContextCompat.getDrawable(context, this)!!.toBitmap()
}

fun @receiver:DrawableRes Int.toDrawable(context: Context, @ColorInt colorInt: Int? = null): Drawable {
    val drawable = ContextCompat.getDrawable(context, this)!!

    return if (colorInt != null) {
        drawable.apply {
            mutate()
            setTint(colorInt)
        }
    } else {
        drawable
    }
}

fun generateLayerDrawable(layers: List<LayerDrawableConfig>): LayerDrawable {
    val layerDrawable = LayerDrawable(layers.map { it.layer }.toTypedArray())

    layers
        .map { it.size }
        .forEachIndexed { index, size ->
            if (size != null) {
                layerDrawable.setLayerSize(index, size, size)
                layerDrawable.setLayerGravity(index, Gravity.CENTER)
            }
        }

    return layerDrawable
}

data class LayerDrawableConfig(
    val layer: Drawable,
    @Px val size: Int? = null,
)

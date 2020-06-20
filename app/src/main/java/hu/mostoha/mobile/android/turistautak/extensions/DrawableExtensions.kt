package hu.mostoha.mobile.android.turistautak.extensions

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

fun @receiver:DrawableRes Int.toBitmap(context: Context): Bitmap {
    return ContextCompat.getDrawable(context, this)!!.toBitmap()
}
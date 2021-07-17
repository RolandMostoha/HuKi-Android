package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

fun @receiver:DrawableRes Int.toBitmap(context: Context): Bitmap {
    return ContextCompat.getDrawable(context, this)!!.toBitmap()
}

fun @receiver:DrawableRes Int.toDrawable(context: Context): Drawable {
    return ContextCompat.getDrawable(context, this)!!
}
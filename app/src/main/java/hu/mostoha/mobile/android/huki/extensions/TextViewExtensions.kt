package hu.mostoha.mobile.android.huki.extensions

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.ui.utils.Message

fun TextView.setDrawableStart(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setDrawableTop(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}

fun TextView.setMessage(message: Message) {
    text = when (message) {
        is Message.Res -> context.getString(message.res, message.formatArgs)
        is Message.Text -> message.text
    }
}

fun TextView.setMessageOrGone(message: Message?) {
    if (message == null) {
        gone()
    } else {
        visible()
        setMessage(message)
    }
}

fun TextView.setTextOrGone(text: String?) {
    if (text == null) {
        gone()
    } else {
        visible()
        this.text = text
    }
}

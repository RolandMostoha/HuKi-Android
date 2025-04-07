package hu.mostoha.mobile.android.huki.extensions

import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.resolve

fun TextView.setDrawableStart(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setDrawableEnd(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

fun TextView.setDrawableTop(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}

fun TextView.clearDrawables() {
    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}

fun TextView.setMessage(message: Message) {
    text = message.resolve(context)
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

fun TextView.setTextOrGone(@StringRes textId: Int?) {
    if (textId == null) {
        gone()
    } else {
        visible()
        this.setText(textId)
    }
}

fun TextView.setTextOrInvisible(text: String?) {
    if (text == null) {
        invisible()
    } else {
        visible()
        this.text = text
    }
}

fun TextView.hyperlinkStyle() {
    setText(
        SpannableString(text).apply {
            setSpan(URLSpan(""), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        },
        TextView.BufferType.SPANNABLE
    )
}

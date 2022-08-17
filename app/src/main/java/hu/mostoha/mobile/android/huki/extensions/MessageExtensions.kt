package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import hu.mostoha.mobile.android.huki.ui.util.Message

fun Message.resolve(context: Context): String {
    return when (val message = this) {
        is Message.Res -> context.getString(message.res, *message.formatArgs.toTypedArray())
        is Message.Text -> message.text
    }
}

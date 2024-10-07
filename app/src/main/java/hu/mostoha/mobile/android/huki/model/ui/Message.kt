package hu.mostoha.mobile.android.huki.model.ui

import android.content.Context
import androidx.annotation.StringRes

sealed class Message {

    data class Text(val text: String) : Message()

    data class Res(
        @StringRes val res: Int,
        val formatArgs: List<Any> = emptyList()
    ) : Message()

}

fun String.toMessage(): Message.Text {
    return Message.Text(this)
}

fun @receiver:StringRes Int.toMessage(): Message.Res {
    return Message.Res(this)
}

fun Message.resolve(context: Context): String {
    return when (val message = this) {
        is Message.Res -> {
            val formatArgs = message.formatArgs
                .map { formatArg ->
                    when (formatArg) {
                        is Message -> formatArg.resolve(context)
                        else -> formatArg
                    }
                }

            if (formatArgs.isNotEmpty()) {
                context.getString(message.res, *formatArgs.toTypedArray())
            } else {
                context.getString(message.res)
            }

        }
        is Message.Text -> message.text
    }
}

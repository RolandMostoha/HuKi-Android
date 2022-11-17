package hu.mostoha.mobile.android.huki.model.ui

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

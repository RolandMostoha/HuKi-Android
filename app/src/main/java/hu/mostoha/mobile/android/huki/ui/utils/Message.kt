package hu.mostoha.mobile.android.huki.ui.utils

import androidx.annotation.StringRes

sealed class Message {

    data class Text(val text: String) : Message()

    data class Res(@StringRes val res: Int) : Message()

}

fun String.toMessage(): Message.Text {
    return Message.Text(this)
}

fun @receiver:StringRes Int.toMessage(): Message.Res {
    return Message.Res(this)
}
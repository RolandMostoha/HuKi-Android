package hu.mostoha.mobile.android.turistautak.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(message: Message) {
    val resolved = when (message) {
        is Message.Text -> message.text
        is Message.Res -> context.getString(message.res)
    }
    Snackbar.make(this, resolved, Snackbar.LENGTH_LONG)
        .show()
}

sealed class Message {
    data class Text(val text: String) : Message()
    data class Res(@StringRes val res: Int) : Message()
}

fun Context.colorStateList(@ColorRes res: Int) = ContextCompat.getColorStateList(this, res)

fun Context.inflateLayout(layoutId: Int, root: ViewGroup? = null, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(this).inflate(layoutId, root, attachToRoot)
}

fun Context.registerReceiver(
    intentFilter: IntentFilter,
    onReceive: (intent: Intent?) -> Unit
): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            onReceive(intent)
        }
    }
    this.registerReceiver(receiver, intentFilter)
    return receiver
}

inline fun <reified T : Any> Context.requireSystemService(): T {
    return ContextCompat.getSystemService(this, T::class.java)
        ?: error("Required System Service was not found: ${T::class.java.simpleName}")
}

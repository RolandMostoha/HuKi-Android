package hu.mostoha.mobile.android.huki.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import hu.mostoha.mobile.android.huki.ui.util.Message

val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.screenWidthPx: Int
    get() {
        return resources.displayMetrics.widthPixels
    }

val Context.screenHeightPx: Int
    get() {
        return resources.displayMetrics.heightPixels
    }

fun Context.showToast(message: Message, length: Int = Toast.LENGTH_LONG) {
    val resolved = when (message) {
        is Message.Text -> message.text
        is Message.Res -> getString(message.res)
    }
    Toast.makeText(this, resolved, length).show()
}

fun Context.showSnackbar(view: View, message: Message) {
    val resolved = when (message) {
        is Message.Text -> message.text
        is Message.Res -> getString(message.res)
    }
    Snackbar.make(view, resolved, Snackbar.LENGTH_LONG).show()
}

fun Context.colorStateList(@ColorRes res: Int) = ContextCompat.getColorStateList(this, res)

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

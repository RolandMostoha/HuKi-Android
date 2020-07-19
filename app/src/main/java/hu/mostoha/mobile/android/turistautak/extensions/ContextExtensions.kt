package hu.mostoha.mobile.android.turistautak.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

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

fun Context.showToast(@StringRes res: Int) = Toast.makeText(this, res, Toast.LENGTH_LONG).show()

fun Context.colorStateList(@ColorRes res: Int) = ContextCompat.getColorStateList(this, res)

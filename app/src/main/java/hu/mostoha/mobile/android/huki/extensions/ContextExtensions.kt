package hu.mostoha.mobile.android.huki.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.resolve

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
    Toast.makeText(this, message.resolve(this), length).show()
}

fun Context.showSnackbar(view: View, message: Message, @DrawableRes icon: Int? = null) {
    val snackbar = Snackbar.make(view, message.resolve(this), Snackbar.LENGTH_LONG)
    val snackbarLayout = snackbar.view
    val textView = snackbarLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    textView.setTextAppearance(R.style.DefaultTextAppearance_SemiBold_Small)
    snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.colorBackground))

    if (icon != null) {
        textView.setDrawableStart(icon)
        textView.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.space_small)
    }

    snackbar.show()
}

fun Context.showErrorSnackbar(view: View, message: Message) {
    showSnackbar(view, message, R.drawable.ic_snackbar_error)
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

fun Context.isDarkMode(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }
}

fun postMain(block: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        block.invoke()
    }
}

fun postMainDelayed(delay: Long, block: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        block.invoke()
    }, delay)
}

package hu.mostoha.mobile.android.huki.extensions

import java.util.Locale
import kotlin.time.Duration

fun Duration.formatHoursAndMinutes(): String {
    return toComponents { hours, minutes, _, _ ->
        String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}

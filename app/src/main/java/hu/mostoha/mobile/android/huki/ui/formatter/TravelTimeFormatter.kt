package hu.mostoha.mobile.android.huki.ui.formatter

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import java.util.Locale
import kotlin.time.Duration

object TravelTimeFormatter {

    fun format(duration: Duration): Message.Res {
        return duration.toComponents { hours, minutes, _, _ ->
            if (hours > 0) {
                Message.Res(R.string.default_travel_time_template_hours_minutes, listOf(hours.toInt(), minutes))
            } else {
                Message.Res(R.string.default_travel_time_template_minutes, listOf(minutes))
            }
        }
    }

    fun formatHoursAndMinutes(duration: Duration): String {
        return duration.toComponents { hours, minutes, _, _ ->
            String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        }
    }

}

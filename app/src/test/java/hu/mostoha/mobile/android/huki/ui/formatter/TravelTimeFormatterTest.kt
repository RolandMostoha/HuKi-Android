package hu.mostoha.mobile.android.huki.ui.formatter

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class TravelTimeFormatterTest {

    @Test
    fun `Given duration with hours, when format, then hours and minutes returns`() {
        val result = TravelTimeFormatter.format(5.hours + 38.minutes)

        assertThat(result).isEqualTo(
            Message.Res(R.string.default_travel_time_template_hours_minutes, listOf(5, 38))
        )
    }

    @Test
    fun `Given duration under an hour, when format, then minutes returns`() {
        val result = TravelTimeFormatter.format(42.minutes)

        assertThat(result).isEqualTo(Message.Res(R.string.default_travel_time_template_minutes, listOf(42)))
    }

    @Test
    fun `Given duration, when formatHoursAndMinutes, then formatted string returns`() {
        val result = TravelTimeFormatter.formatHoursAndMinutes(3299693.milliseconds)

        assertThat(result).isEqualTo("00:54")
    }

}

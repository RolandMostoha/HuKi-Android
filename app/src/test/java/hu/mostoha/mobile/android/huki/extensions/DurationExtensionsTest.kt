package hu.mostoha.mobile.android.huki.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class DurationExtensionsTest {

    @Test
    fun `Given duration, when formatHoursAndMinutes, then formatted string returns`() {
        val duration = 3299693.milliseconds

        val formatted = duration.formatHoursAndMinutes()

        assertThat(formatted).isEqualTo("00:54")
    }

}

package hu.mostoha.mobile.android.huki.ui.util

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import org.junit.Test

class DistanceFormatterTest {

    @Test
    fun `Given distance below 1000m, when format, then meters template is used`() {
        val distance = 500

        val formatted = DistanceFormatter.format(distance)

        assertThat(formatted)
            .isEqualTo(Message.Res(R.string.default_distance_template_m, listOf(distance)))
    }

    @Test
    fun `Given distance above 1000m, when format, then km template is used`() {
        val distance = 1500

        val formatted = DistanceFormatter.format(distance)

        assertThat(formatted)
            .isEqualTo(Message.Res(R.string.default_distance_template_km, listOf(1.5)))
    }

}

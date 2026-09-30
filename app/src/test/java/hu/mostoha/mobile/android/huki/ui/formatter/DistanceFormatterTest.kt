package hu.mostoha.mobile.android.huki.ui.formatter

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import org.junit.Test

class DistanceFormatterTest {

    @Test
    fun `Given meters, when formatRelative, then plus prefixed meters returns`() {
        val result = DistanceFormatter.formatRelative(50)

        assertThat(result).isEqualTo(Message.Res(R.string.default_distance_template_m, listOf("+50")))
    }

    @Test
    fun `Given kilometers, when formatRelative, then plus prefixed kilometers returns`() {
        val result = DistanceFormatter.formatRelative(8_040)

        assertThat(result).isEqualTo(Message.Res(R.string.default_distance_template_km, listOf("+8")))
    }

}

package hu.mostoha.mobile.android.huki.ui.formatter

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.ui.Message
import org.junit.Test
import org.osmdroid.util.GeoPoint

class LocationFormatterTest {

    @Test
    fun `Given location, when format, then separated coordinates are returned`() {
        val location = Location(47.819483, 19.134789)

        val result = LocationFormatter.formatText(location)

        assertThat(result).isEqualTo(Message.Text("(47.8194,19.1347)"))
    }

    @Test
    fun `Given geoPoint, when format, then separated coordinates are returned`() {
        val location = GeoPoint(47.819483, 19.134789)

        val result = LocationFormatter.formatText(location)

        assertThat(result).isEqualTo(Message.Text("(47.8194,19.1347)"))
    }

}

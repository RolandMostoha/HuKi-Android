package hu.mostoha.mobile.android.huki.util

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Location
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TravelTimeUtilsTest {

    @Test
    fun `Given 5 km distance without incline, when naismith, then 1 hour travel time returns`() {
        val travelTime = naismith(5.0, 0.0)

        assertThat(travelTime).isEqualTo(1.25)
    }

    @Test
    fun `Given list of locations, when calculateTravelTime, then estimated travel time returns`() {
        val locations = listOf(
            Location(47.123, 19.234, 90.0),
            Location(47.123, 19.235, 100.0),
            Location(47.123, 19.236, 100.0),
            Location(47.123, 19.237, 90.0),
        )

        val travelTime = locations.calculateTravelTime()

        assertThat(travelTime).isEqualTo(4.minutes.plus(25.2.seconds))
    }

}

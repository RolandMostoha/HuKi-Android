package hu.mostoha.mobile.android.huki.util

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Location
import org.junit.Test

class LocationUtilsTest {

    @Test
    fun `Given two locations, when distanceBetween, then distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)

        val distance = from.distanceBetween(to)

        assertThat(distance).isEqualTo(62_416)
    }

    @Test
    fun `Given list of locations, when calculateDistance, then the total distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)
        val locations = listOf(from, to, from, to)

        val distance = locations.calculateDistance()

        assertThat(distance).isEqualTo(3 * 62_416)
    }

    @Test
    fun `Given list of locations, when calculateCenter, then the center location returns`() {
        val location1 = Location(47.123, 19.234)
        val location2 = Location(46.567, 19.345)
        val locations = listOf(location1, location2)

        val center = locations.calculateCenter()

        assertThat(center).isEqualTo(
            Location(
                latitude = (location1.latitude + location2.latitude) / 2,
                longitude = (location1.longitude + location2.longitude) / 2,
            )
        )
    }

}

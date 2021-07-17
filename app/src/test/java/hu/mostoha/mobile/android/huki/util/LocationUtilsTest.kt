package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationUtilsTest {

    @Test
    fun `Given two locations, when distanceBetween, then distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)

        val distance = from.distanceBetween(to)

        assertEquals(62_416, distance)
    }

    @Test
    fun `Given list of locations, when calculateDistance, then distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)
        val locations = listOf(from, to, from, to)

        val distance = locations.calculateDistance()

        assertEquals(3 * 62_416, distance)
    }

}
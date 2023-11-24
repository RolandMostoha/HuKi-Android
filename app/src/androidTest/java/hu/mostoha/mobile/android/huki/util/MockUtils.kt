package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.Location
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
import android.location.Location as AndroidLocation

fun Location.toMockLocation(): AndroidLocation {
    val mockLocation = mockk<AndroidLocation>(relaxed = true)

    every { mockLocation.latitude } returns this.latitude
    every { mockLocation.longitude } returns this.longitude
    every { mockLocation.altitude } returns (this.altitude ?: 0.0)
    every { mockLocation.accuracy } returns (Random.nextFloat())

    return mockLocation
}

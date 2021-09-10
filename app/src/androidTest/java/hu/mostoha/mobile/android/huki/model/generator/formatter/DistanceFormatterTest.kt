package hu.mostoha.mobile.android.huki.model.generator.formatter

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import hu.mostoha.mobile.android.huki.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DistanceFormatterTest {

    private lateinit var context: Context
    private lateinit var distanceFormatter: DistanceFormatter

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        distanceFormatter = DistanceFormatter(context)
    }

    @Test
    fun givenBelow1000_whenFormat_thenMetersTemplateUsed() {
        val distance = 500

        val formatted = distanceFormatter.format(distance)

        assertEquals(
            context.getString(R.string.default_distance_template_m, 500),
            formatted
        )
    }

    @Test
    fun givenAbove1000_whenFormat_thenKiloMetersTemplateUsed() {
        val distance = 1500

        val formatted = distanceFormatter.format(distance)

        assertEquals(
            context.getString(R.string.default_distance_template_km, 1.5),
            formatted
        )
    }

}
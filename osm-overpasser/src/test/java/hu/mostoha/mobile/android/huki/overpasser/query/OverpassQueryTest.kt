package hu.mostoha.mobile.android.huki.overpasser.query

import hu.mostoha.mobile.android.huki.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.huki.overpasser.output.OutputModificator
import hu.mostoha.mobile.android.huki.overpasser.output.OutputOrder
import hu.mostoha.mobile.android.huki.overpasser.output.OutputVerbosity
import org.junit.Assert.assertEquals
import org.junit.Test

class OverpassQueryTest {

    @Test
    fun `Given empty query, when build then quotes are removed from settings part`() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(30)
            .output(OutputVerbosity.TAGS, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()

        assertEquals("[out:json][timeout:30]; out tags center qt 20;", query)
    }

    @Test
    fun `Given relBy query, when build, then relation id is in brackets`() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_SEC)
            .filterQuery()
            .relBy("4640869")
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()

        assertEquals("[out:json][timeout:30]; (rel(4640869);<;); out tags center qt 20;", query)
    }

    @Test
    fun `Given case insensitive query, when build, then 'i' is appended`() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(TIMEOUT_SEC)
            .filterQuery()
            .rel()
            .tagRegex("name", "Mec", false)
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()

        assertEquals("[out:json][timeout:30]; (rel[\"name\"~\"Mec\",i];<;); out tags center qt 20;", query)
    }

    companion object {
        private const val TIMEOUT_SEC = 30
    }

}

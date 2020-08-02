package hu.mostoha.mobile.android.turistautak.network.model

import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputModificator
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputOrder
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.turistautak.network.overpasser.query.OverpassQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class OverpassQueryTest {

    @Test
    fun givenBasicQuery_whenBuild_thenQuotesAreRemovedFromSettings() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(30)
            .output(OutputVerbosity.TAGS, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()

        assertEquals("[out:json][timeout:30]; out tags center qt 20;", query)
    }

    @Test
    fun givenRelByQuery_whenBuild_thenRelationIdIsInBrackets() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .relBy("4640869")
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()

        assertEquals("[out:json][timeout:30]; (rel(4640869);<;); out tags center qt 20;", query)
    }

    @Test
    fun givenCaseInsensitiveQuery_whenBuild_thenIisAppended() {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .rel()
            .tagRegex("name", "Mec", false)
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()

        assertEquals("[out:json][timeout:30]; (rel[\"name\"~\"Mec\",i];<;); out tags center qt 20;", query)
    }

}
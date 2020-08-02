package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.constants.HUNGARY_BOUNDING_BOX
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.model.OverpassQueryResult
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.turistautak.network.overpasser.query.OverpassQuery
import javax.inject.Inject

class OverpassRepositoryImpl @Inject constructor(
    private val overpassService: OverpassService
) : OverpassRepository {

    override suspend fun getHikingRelationsBy(searchText: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .rel()
            .tag("type", "route")
            .tag("route", "hiking")
            .tagRegex("name", searchText, false)
            .boundingBox(
                HUNGARY_BOUNDING_BOX.south,
                HUNGARY_BOUNDING_BOX.west,
                HUNGARY_BOUNDING_BOX.north,
                HUNGARY_BOUNDING_BOX.east
            )
            .end()
            .output(OutputVerbosity.TAGS, null, null, 20)
            .build()

        return overpassService.interpreter(query)
    }

    override suspend fun getNodesByRelationId(id: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .relBy(id)
            .wayBy("r")
            .nodeBy("w")
            .end()
            .output(OutputVerbosity.SKEL, null, null, 1000)
            .build()

        return overpassService.interpreter(query)
    }

}
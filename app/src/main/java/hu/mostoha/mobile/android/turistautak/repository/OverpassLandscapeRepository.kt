package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.constants.HUNGARY
import hu.mostoha.mobile.android.turistautak.model.domain.Landscape
import hu.mostoha.mobile.android.turistautak.model.generator.LandscapeDomainModelGenerator
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.turistautak.network.overpasser.query.OverpassQuery
import javax.inject.Inject

class OverpassLandscapeRepository @Inject constructor(
    private val overpassService: OverpassService,
    private val modelGenerator: LandscapeDomainModelGenerator
) : LandscapeRepository {

    override suspend fun getLandscapes(): List<Landscape> {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_SEC)
            .filterQuery()
            .way()
            .tag("natural", "mountain_range")
            .boundingBox(
                HUNGARY.south,
                HUNGARY.west,
                HUNGARY.north,
                HUNGARY.east
            )
            .end()
            .output(OutputVerbosity.TAGS, null, null, -1)
            .build()
        val response = overpassService.interpreter(query)
        return modelGenerator.generateLandscapes(response)
    }

}

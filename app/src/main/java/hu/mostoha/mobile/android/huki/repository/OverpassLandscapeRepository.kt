package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.generator.LandscapeDomainModelGenerator
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.huki.network.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.huki.network.overpasser.query.OverpassQuery
import hu.mostoha.mobile.android.huki.util.HUNGARY
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

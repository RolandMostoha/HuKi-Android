package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.constants.HUNGARY_BOUNDING_BOX
import hu.mostoha.mobile.android.turistautak.extensions.fixQueryErrors
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.model.OverpassQueryResult
import hu.supercluster.overpasser.library.output.OutputFormat
import hu.supercluster.overpasser.library.output.OutputModificator
import hu.supercluster.overpasser.library.output.OutputOrder
import hu.supercluster.overpasser.library.output.OutputVerbosity
import hu.supercluster.overpasser.library.query.OverpassQuery
import javax.inject.Inject

class OverpassRepositoryImpl @Inject constructor(
    private val overpassService: OverpassService
) : OverpassRepository {

    override suspend fun searchHikingRelationsBy(searchText: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(10)
            .filterQuery()
            .rel()
            .tag("route", "hiking")
            .tag("jel")
            .tagRegex("name", searchText)
            .boundingBox(
                HUNGARY_BOUNDING_BOX.south,
                HUNGARY_BOUNDING_BOX.west,
                HUNGARY_BOUNDING_BOX.north,
                HUNGARY_BOUNDING_BOX.east
            )
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.CENTER, OutputOrder.QT, 20)
            .build()
            .fixQueryErrors()

        return overpassService.interpreter(query)
    }

}
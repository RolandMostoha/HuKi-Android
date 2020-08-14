package hu.mostoha.mobile.android.turistautak.model.generator

import hu.mostoha.mobile.android.turistautak.model.domain.Landscape
import hu.mostoha.mobile.android.turistautak.model.domain.LandscapeType
import hu.mostoha.mobile.android.turistautak.model.network.OverpassQueryResponse
import javax.inject.Inject

class LandscapeDomainModelGenerator @Inject constructor() {

    fun generateLandscapes(queryResponse: OverpassQueryResponse): List<Landscape> {
        return queryResponse.elements.mapNotNull {
            Landscape(
                id = it.id.toString(),
                name = it.tags?.nameHungarian ?: it.tags?.name ?: return@mapNotNull null,
                type = LandscapeType.MOUNTAIN_RANGE_HIGH
            )
        }
    }

}
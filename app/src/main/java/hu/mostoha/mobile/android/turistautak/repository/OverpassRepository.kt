package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.network.OverpassQueryResult

interface OverpassRepository {
    suspend fun getHikingRelationsBy(searchText: String): OverpassQueryResult
    suspend fun getNodesByRelationId(relationId: Long): OverpassQueryResult
}

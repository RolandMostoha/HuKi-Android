package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.data.landscapes
import hu.mostoha.mobile.android.turistautak.model.domain.Landscape
import javax.inject.Inject

class LocalLandscapeRepository @Inject constructor() : LandscapeRepository {

    override suspend fun getLandscapes(): List<Landscape> {
        return landscapes
    }

}

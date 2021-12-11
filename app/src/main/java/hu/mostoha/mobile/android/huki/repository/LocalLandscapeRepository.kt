package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.data.landscapes
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import javax.inject.Inject

class LocalLandscapeRepository @Inject constructor() : LandscapeRepository {

    override suspend fun getLandscapes(): List<Landscape> = landscapes

}

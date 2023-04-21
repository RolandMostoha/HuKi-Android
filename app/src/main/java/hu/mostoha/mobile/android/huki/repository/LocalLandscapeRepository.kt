package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import javax.inject.Inject

class LocalLandscapeRepository @Inject constructor() : LandscapeRepository {

    override suspend fun getLandscapes(): List<Landscape> = LOCAL_LANDSCAPES

}

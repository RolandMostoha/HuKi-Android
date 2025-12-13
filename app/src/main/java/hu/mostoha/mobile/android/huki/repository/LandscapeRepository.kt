package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.Location

interface LandscapeRepository {

    fun getLandscapes(location: Location? = null): List<Landscape>

    suspend fun getLandscapeGeometryList(): List<Pair<Landscape, Geometry>>

}

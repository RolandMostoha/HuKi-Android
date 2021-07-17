package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.Landscape

interface LandscapeRepository {
    suspend fun getLandscapes(): List<Landscape>
}
package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.Landscape

interface LandscapeRepository {
    suspend fun getLandscapes(): List<Landscape>
}
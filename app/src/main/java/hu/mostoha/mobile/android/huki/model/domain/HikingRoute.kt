package hu.mostoha.mobile.android.huki.model.domain

import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType

data class HikingRoute(
    val osmId: String,
    val name: String,
    val symbolType: SymbolType
)

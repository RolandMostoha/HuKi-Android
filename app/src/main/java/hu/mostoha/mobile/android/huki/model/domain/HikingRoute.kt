package hu.mostoha.mobile.android.huki.model.domain

import hu.mostoha.mobile.android.huki.model.network.SymbolType

data class HikingRoute(
    val id: String,
    val name: String,
    val symbolType: SymbolType
)
package hu.mostoha.mobile.android.turistautak.model.domain

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.turistautak.R

data class HikingRoute(
    val id: String,
    val name: String,
    val symbolType: SymbolType
)

enum class SymbolType : IconEnum {
    K {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_z
    },
    Z {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_z
    }
}

interface IconEnum {
    @DrawableRes
    fun getIconRes(): Int
}

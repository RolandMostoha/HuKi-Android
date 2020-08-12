package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.model.domain.Landscape
import javax.inject.Inject

class LandscapeRepository @Inject constructor() {

    fun getLandscapes(): List<Landscape> {
        return listOf(
            Landscape(
                "Mecsek",
                R.drawable.ic_landscapes_mountain
            ),
            Landscape(
                "Balaton",
                R.drawable.ic_landscapes_water
            ),
            Landscape(
                "Mátra",
                R.drawable.ic_landscapes_mountain
            ),
            Landscape(
                "Szilvásvárad",
                R.drawable.ic_landscapes_city
            ),
            Landscape(
                "Pilis",
                R.drawable.ic_landscapes_mountain
            )
        )
    }

}
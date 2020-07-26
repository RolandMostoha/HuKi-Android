package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.network.model.Element
import javax.inject.Inject

class SearchBarUiModelGenerator @Inject constructor() {

    fun generate(elements: List<Element>): List<SearchResultItem> {
        return elements.map {
            // TODO: use proper symbol icons
            SearchResultItem(
                it.tags.name,
                R.drawable.ic_sign_k
            )
        }
    }

}
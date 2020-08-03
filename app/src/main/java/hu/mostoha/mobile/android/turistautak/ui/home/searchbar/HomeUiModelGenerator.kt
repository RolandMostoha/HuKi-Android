package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.network.model.Element
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class HomeUiModelGenerator @Inject constructor() {

    fun generateSearchResult(elements: List<Element>): List<SearchResultUiModel> {
        return elements.mapNotNull {
            it.tags?.name?.let { name ->
                SearchResultUiModel(
                    it.id,
                    name,
                    // TODO: use proper symbol icons
                    R.drawable.ic_sign_k
                )
            }
        }
    }

    fun generateNodes(elements: List<Element>): List<NodeUiModel> {
        return elements.mapNotNull {
            val lat = it.lat
            val lon = it.lon
            if (lat != null && lon != null) {
                NodeUiModel(
                    it.id,
                    GeoPoint(lat, lon)
                )
            } else {
                null
            }
        }
    }

}

data class NodeUiModel(val id: Long, val geoPoint: GeoPoint)

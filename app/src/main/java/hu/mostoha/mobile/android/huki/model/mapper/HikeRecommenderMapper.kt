package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.util.KIRANDULASTIPPEK_QUERY_URL
import hu.mostoha.mobile.android.huki.util.KIRANDULASTIPPEK_URL
import hu.mostoha.mobile.android.huki.util.TERMESZETJARO_AREA_URL
import hu.mostoha.mobile.android.huki.util.TERMESZETJARO_PLACE_URL
import hu.mostoha.mobile.android.huki.util.TERMESZETJARO_URL
import hu.mostoha.mobile.android.huki.util.distanceBetween
import java.net.URLEncoder

object HikeRecommenderMapper {

    fun getKirandulastippekLink(placeArea: PlaceArea): String {
        val closestLandscape = LOCAL_LANDSCAPES
            .map { it to it.center.distanceBetween(placeArea.location) }
            .minBy { it.second }
            .first

        return if (closestLandscape.kirandulastippekTag != null) {
            KIRANDULASTIPPEK_QUERY_URL.format(closestLandscape.kirandulastippekTag)
        } else {
            KIRANDULASTIPPEK_URL
        }
    }

    fun getTermeszetjaroLink(placeArea: PlaceArea): String {
        return when (placeArea.placeAreaType) {
            PlaceAreaType.PLACE_DETAILS -> {
                TERMESZETJARO_PLACE_URL.format(
                    "${placeArea.location.longitude},${placeArea.location.latitude}"
                )
            }
            else -> {
                val closestLandscape = LOCAL_LANDSCAPES
                    .map { it to it.center.distanceBetween(placeArea.location) }
                    .minBy { it.second }
                    .first

                if (closestLandscape.termeszetjaroTag != null) {
                    TERMESZETJARO_AREA_URL.format(
                        closestLandscape.termeszetjaroTag.areaId,
                        URLEncoder.encode(closestLandscape.termeszetjaroTag.areaName, "UTF-8")
                    )
                } else {
                    TERMESZETJARO_URL
                }
            }
        }
    }

}

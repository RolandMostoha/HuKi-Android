package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.util.TERMESZETJARO_PLACE_URL
import hu.mostoha.mobile.android.huki.util.distanceBetween
import java.net.URLEncoder

object HikeRecommendationMapper {

    fun getNavigationLink(hikeRecommendation: HikeRecommendation, placeArea: PlaceArea): String {
        return when (hikeRecommendation) {
            HikeRecommendation.TERMESZETJARO -> getTermeszetjaroLink(placeArea)
            else -> {
                val closestLandscape = LOCAL_LANDSCAPES
                    .map { it to it.center.distanceBetween(placeArea.location) }
                    .minBy { it.second }
                    .first

                val areaTag = closestLandscape.areaTags[hikeRecommendation]

                return if (areaTag != null) {
                    hikeRecommendation.areaUrl.format(areaTag)
                } else {
                    hikeRecommendation.baseUrl
                }
            }
        }
    }

    private fun getTermeszetjaroLink(placeArea: PlaceArea): String {
        return when (placeArea.placeAreaType) {
            PlaceAreaType.PlaceDetails -> {
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
                    HikeRecommendation.TERMESZETJARO.areaUrl.format(
                        closestLandscape.termeszetjaroTag.areaId,
                        URLEncoder.encode(closestLandscape.termeszetjaroTag.areaName, "UTF-8")
                    )
                } else {
                    HikeRecommendation.TERMESZETJARO.baseUrl
                }
            }
        }
    }

}

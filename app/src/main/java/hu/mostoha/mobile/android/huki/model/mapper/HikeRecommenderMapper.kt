package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.constants.KIRANDULASTIPPEK_QUERY_URL
import hu.mostoha.mobile.android.huki.constants.KIRANDULASTIPPEK_URL
import hu.mostoha.mobile.android.huki.constants.TERMESZETJARO_AREA_URL
import hu.mostoha.mobile.android.huki.constants.TERMESZETJARO_PLACE_URL
import hu.mostoha.mobile.android.huki.constants.TERMESZETJARO_URL
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.util.distanceBetween
import org.osmdroid.util.BoundingBox
import java.net.URLEncoder

object HikeRecommenderMapper {

    fun map(landscapeUiModel: LandscapeUiModel, boundingBox: BoundingBox): HikeRecommendation {
        val landscape = LOCAL_LANDSCAPES.first { it.osmId == landscapeUiModel.osmId }

        return HikeRecommendation(
            title = Message.Res(
                R.string.hike_recommender_landscape_title_template,
                listOf(landscapeUiModel.name),
            ),
            iconRes = landscapeUiModel.iconRes,
            hikingRoutesBoundingBox = boundingBox.toDomain(),
            kirandulastippekLink = landscape.getKirandulastippekLink(),
            termeszetjaroLink = landscape.getTermeszetjaroLink(),
        )
    }

    fun map(placeUiModel: PlaceUiModel, boundingBox: BoundingBox): HikeRecommendation {
        val geoPoint = placeUiModel.geoPoint
        val closestLandscape = LOCAL_LANDSCAPES
            .map { it to it.center.distanceBetween(geoPoint.toLocation()) }
            .minBy { it.second }
            .first

        return HikeRecommendation(
            title = Message.Res(
                R.string.hike_recommender_place_title_template,
                listOf(placeUiModel.primaryText),
            ),
            iconRes = placeUiModel.iconRes,
            hikingRoutesBoundingBox = boundingBox.toDomain(),
            kirandulastippekLink = closestLandscape.getKirandulastippekLink(),
            termeszetjaroLink = TERMESZETJARO_PLACE_URL.format(
                "${geoPoint.longitude},${geoPoint.latitude}"
            ),
        )
    }

    private fun Landscape.getKirandulastippekLink(): String {
        return if (kirandulastippekTag != null) {
            KIRANDULASTIPPEK_QUERY_URL.format(kirandulastippekTag)
        } else {
            KIRANDULASTIPPEK_URL
        }
    }

    private fun Landscape.getTermeszetjaroLink(): String {
        return if (termeszetjaroTag != null) {
            TERMESZETJARO_AREA_URL.format(
                termeszetjaroTag.areaId,
                URLEncoder.encode(termeszetjaroTag.areaName, "UTF-8")
            )
        } else {
            TERMESZETJARO_URL
        }
    }

}

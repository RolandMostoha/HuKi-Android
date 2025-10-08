package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_AREA_LOCATION
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.toTestPlaceArea
import org.junit.Test
import java.net.URLEncoder

class HikeRecommendationMapperTest {

    private val mapper = HikeRecommendationMapper

    @Test
    fun `Given TERMESZETJARO and PLACE_DETAILS area type, when getNavigationLink, then TERMESZETJARO_PLACE_URL is returned`() {
        val placeArea = DEFAULT_NODE_NAME
            .toTestPlaceArea()
            .copy(placeAreaType = PlaceAreaType.PLACE_DETAILS)

        val actualUrl = mapper.getNavigationLink(HikeRecommendation.TERMESZETJARO, placeArea)

        assertThat(actualUrl).contains(DEFAULT_PLACE_AREA_LOCATION.latitude.toString())
        assertThat(actualUrl).contains(DEFAULT_PLACE_AREA_LOCATION.longitude.toString())
        assertThat(actualUrl).contains(HikeRecommendation.TERMESZETJARO.baseUrl)
    }

    @Test
    fun `Given TERMESZETJARO and MAP_SEARCH area type, when getNavigationLink, then TERMESZETJARO_AREA_URL is returned`() {
        val placeArea = DEFAULT_NODE_NAME
            .toTestPlaceArea()
            .copy(placeAreaType = PlaceAreaType.MAP_SEARCH)
        val closestLandscape = LOCAL_LANDSCAPES
            .map { it to it.center.distanceBetween(placeArea.location) }
            .minBy { it.second }
            .first

        val actualUrl = mapper.getNavigationLink(HikeRecommendation.TERMESZETJARO, placeArea)

        assertThat(actualUrl).isEqualTo(
            HikeRecommendation.TERMESZETJARO.areaUrl.format(
                closestLandscape.termeszetjaroTag!!.areaId,
                URLEncoder.encode(closestLandscape.termeszetjaroTag.areaName, "UTF-8"),
            )
        )
    }

    @Test
    fun `Given AKTIVKALANDOR and MAP_SEARCH area type, when getNavigationLink, then AKTIVKALANDOR URL is returned`() {
        val placeArea = DEFAULT_NODE_NAME
            .toTestPlaceArea()
            .copy(placeAreaType = PlaceAreaType.MAP_SEARCH)
        val closestLandscape = LOCAL_LANDSCAPES
            .map { it to it.center.distanceBetween(placeArea.location) }
            .minBy { it.second }
            .first

        val actualUrl = mapper.getNavigationLink(HikeRecommendation.AKTIVKALANDOR, placeArea)

        assertThat(actualUrl).isEqualTo(
            HikeRecommendation.AKTIVKALANDOR.areaUrl.format(
                closestLandscape.areaTags[HikeRecommendation.AKTIVKALANDOR],
            )
        )
    }

}

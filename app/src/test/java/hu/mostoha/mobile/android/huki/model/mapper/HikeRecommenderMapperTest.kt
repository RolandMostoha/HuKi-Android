package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.constants.KIRANDULASTIPPEK_QUERY_URL
import hu.mostoha.mobile.android.huki.constants.TERMESZETJARO_AREA_URL
import hu.mostoha.mobile.android.huki.constants.TERMESZETJARO_PLACE_URL
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.ui.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import org.junit.Test
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.net.URLEncoder

class HikeRecommenderMapperTest {

    private val mapper = HikeRecommenderMapper

    @Test
    fun `Given landscape with bounding box, when map, then hike recommendation returns`() {
        val osmId = "3716160"
        val landscape = LOCAL_LANDSCAPES.first { it.osmId == osmId }
        val landscapeUiModel = LandscapeUiModel(
            osmId = osmId,
            osmType = PlaceType.RELATION,
            name = R.string.landscape_budai_hegyseg.toMessage(),
            geoPoint = GeoPoint(47.5428510, 18.9236294),
            iconRes = R.drawable.ic_landscapes_cave,
            markerRes = R.drawable.ic_marker_landscapes_forest,
        )
        val boundingBox = BoundingBox(1.0, 2.0, 3.0, 4.0)

        val result = mapper.map(landscapeUiModel, boundingBox)

        assertThat(result).isEqualTo(
            HikeRecommendation(
                title = Message.Res(
                    R.string.hike_recommender_landscape_title_template,
                    listOf(landscapeUiModel.name),
                ),
                iconRes = R.drawable.ic_landscapes_cave,
                hikingRoutesBoundingBox = boundingBox.toDomain(),
                kirandulastippekLink = KIRANDULASTIPPEK_QUERY_URL.format(landscape.kirandulastippekTag),
                termeszetjaroLink = TERMESZETJARO_AREA_URL.format(
                    landscape.termeszetjaroTag!!.areaId,
                    URLEncoder.encode(landscape.termeszetjaroTag!!.areaName, "UTF-8")
                ),
            ),
        )
    }

    @Test
    fun `Given place with bounding box, when map, then hike recommendation returns`() {
        val placeUiModel = DEFAULT_PLACE_UI_MODEL
        val boundingBox = BoundingBox(1.0, 2.0, 3.0, 4.0)
        val closestLandscape = LOCAL_LANDSCAPES.first { it.nameRes == R.string.landscape_visegrádi_hegység }

        val result = mapper.map(placeUiModel, boundingBox)

        assertThat(result).isEqualTo(
            HikeRecommendation(
                title = Message.Res(
                    R.string.hike_recommender_place_title_template,
                    listOf(placeUiModel.primaryText),
                ),
                iconRes = placeUiModel.iconRes,
                hikingRoutesBoundingBox = boundingBox.toDomain(),
                kirandulastippekLink = KIRANDULASTIPPEK_QUERY_URL.format(closestLandscape.kirandulastippekTag),
                termeszetjaroLink = TERMESZETJARO_PLACE_URL.format(
                    "${placeUiModel.geoPoint.longitude},${placeUiModel.geoPoint.latitude}"
                ),
            ),
        )
    }

    private companion object {
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            primaryText = DEFAULT_NODE_NAME.toMessage(),
            secondaryText = DEFAULT_NODE_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.MAP_SEARCH,
            boundingBox = null,
        )
    }
}

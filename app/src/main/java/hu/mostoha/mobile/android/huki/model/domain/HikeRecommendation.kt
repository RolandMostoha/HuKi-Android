package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage

enum class HikeRecommendation(
    val title: Message.Res,
    @DrawableRes val iconRes: Int,
    val baseUrl: String,
    val areaUrl: String,
) {
    AKTIVKALANDOR(
        title = R.string.hike_recommender_aktivkalandor.toMessage(),
        iconRes = R.drawable.ic_hike_recommender_aktivkalandor,
        baseUrl = "https://aktivkalandor.hu/turak-a-terkepen/",
        areaUrl = "https://aktivkalandor.hu/tajegysegek/%s"
    ),
    KIRANDULASTIPPEK(
        title = R.string.hike_recommender_kirandulastippek.toMessage(),
        iconRes = R.drawable.ic_hike_recommender_kirandulastippek,
        baseUrl = "https://kirandulastippek.hu",
        areaUrl = "https://kirandulastippek.hu/%s?tag=gyalogtura"
    ),
    TERMESZETJARO(
        title = R.string.hike_recommender_termeszetjaro.toMessage(),
        iconRes = R.drawable.ic_hike_recommender_termeszetjaro,
        baseUrl = "https://www.termeszetjaro.hu",
        areaUrl = "https://www.termeszetjaro.hu/hu/tours/?cat=22729870#area=%s&wt=%s",
    ),
}

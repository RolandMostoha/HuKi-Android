package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox

data class HikeRecommendation(
    val title: Message,
    @DrawableRes val iconRes: Int,
    val hikingRoutesBoundingBox: BoundingBox,
    val kirandulastippekLink: String,
    val termeszetjaroLink: String,
)

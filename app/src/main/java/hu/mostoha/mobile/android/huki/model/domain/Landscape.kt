package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

data class Landscape(
    val osmId: String,
    val osmType: PlaceType,
    @StringRes val nameRes: Int,
    val landscapeType: LandscapeType,
    val center: Location,
    @ColorRes val color: Int,
    val destinations: List<Destination>,
    val areaTags: Map<HikeRecommendation, String>,
    val termeszetjaroTag: TermeszetjaroTag? = null,
)

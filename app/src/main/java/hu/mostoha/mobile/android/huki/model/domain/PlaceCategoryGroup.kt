package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.R

enum class PlaceCategoryGroup(@StringRes val title: Int) {
    NATURE(R.string.place_category_group_nature),
    ATTRACTIONS(R.string.place_category_group_attractions),
    SHOPS(R.string.place_category_group_shops),
    TRAVEL(R.string.place_category_group_travel),
    USEFUL(R.string.place_category_group_useful)
}

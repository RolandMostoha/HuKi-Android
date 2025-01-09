package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message

const val OSM_TAG_NAME = "name"

enum class OsmTags(
    val osmKey: String,
    @StringRes val title: Int,
    val valueResolver: ((String) -> Message)? = null
) {
    ELE("ele", R.string.place_category_osm_tag_ele),
    WEBSITE("website", R.string.place_category_osm_tag_website),
    URL("url", R.string.place_category_osm_tag_url),
    DESCRIPTION("description", R.string.place_category_osm_tag_description),
    INSCRIPTION("inscription", R.string.place_category_osm_tag_inscription),
    OPENING_HOURS("opening_hours", R.string.place_category_osm_tag_opening_hours),
    OPERATOR("operator", R.string.place_category_osm_tag_operator),
    CAPACITY("capacity", R.string.place_category_osm_tag_capacity),
    FEE("fee", R.string.place_category_osm_tag_fee, {
        when (it) {
            "yes" -> Message.Res(R.string.osm_tag_yes)
            "no" -> Message.Res(R.string.osm_tag_no)
            else -> Message.Text(it)
        }
    }),
    REF("ref", R.string.place_category_osm_tag_ref),
    ROUTE_REF("route_ref", R.string.place_category_osm_tag_ref),
    RELIGION("religion", R.string.place_category_osm_tag_religion, {
        when (it) {
            "christian" -> Message.Res(R.string.osm_tag_christian)
            "muslim" -> Message.Res(R.string.osm_tag_muslim)
            "jewish" -> Message.Res(R.string.osm_tag_jewish)
            "buddhist" -> Message.Res(R.string.osm_tag_buddhist)
            "hindu" -> Message.Res(R.string.osm_tag_hindu)
            "pagan" -> Message.Res(R.string.osm_tag_pagan)
            else -> Message.Text(it)
        }
    }),
    DENOMINATION("denomination", R.string.place_category_osm_tag_denomination, {
        when (it) {
            "catholic" -> Message.Res(R.string.osm_tag_catholic)
            "roman_catholic" -> Message.Res(R.string.osm_tag_roman_catholic)
            "greek_catholic" -> Message.Res(R.string.osm_tag_greek_catholic)
            "protestant" -> Message.Res(R.string.osm_tag_protestant)
            "reformed" -> Message.Res(R.string.osm_tag_reformed)
            else -> Message.Text(it)
        }
    }),
    JEL("jel", R.string.place_category_osm_tag_jel),
}

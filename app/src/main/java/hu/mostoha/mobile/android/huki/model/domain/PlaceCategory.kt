package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage

enum class PlaceCategory(
    val osmQueryTags: List<Pair<String, String?>>,
    val importantOsmTags: List<OsmTags>,
    val categoryGroup: PlaceCategoryGroup,
    val title: Message.Res,
    @DrawableRes val iconRes: Int,
    @ColorRes val categoryColorRes: Int,
) {
    PEAK(
        osmQueryTags = listOf("natural" to "peak"),
        importantOsmTags = listOf(OsmTags.ELE, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = R.string.place_category_peak.toMessage(),
        iconRes = R.drawable.ic_place_category_peak,
        categoryColorRes = R.color.colorPlaceCategoryPeak,
    ),
    FOREST(
        osmQueryTags = listOf(
            "landuse" to "forest",
            "natural" to "wood",
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = R.string.place_category_forest.toMessage(),
        iconRes = R.drawable.ic_place_category_forest,
        categoryColorRes = R.color.colorPlaceCategoryForest,
    ),
    RIVER(
        osmQueryTags = listOf(
            "waterway" to "river",
            "waterway" to "stream",
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = R.string.place_category_river.toMessage(),
        iconRes = R.drawable.ic_place_category_river,
        categoryColorRes = R.color.colorPlaceCategoryRiver,
    ),
    WATERFALL(
        osmQueryTags = listOf("waterway" to "waterfall"),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = R.string.place_category_waterfall.toMessage(),
        iconRes = R.drawable.ic_place_category_waterfall,
        categoryColorRes = R.color.colorPlaceCategoryWaterfall,
    ),
    CAVE(
        osmQueryTags = listOf("natural" to "cave_entrance"),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = R.string.place_category_cave.toMessage(),
        iconRes = R.drawable.ic_landscapes_cave,
        categoryColorRes = R.color.colorPlaceCategoryCave,
    ),
    ROCK(
        osmQueryTags = listOf(
            "natural" to "rock",
            "natural" to "cliff",
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = R.string.place_category_rock.toMessage(),
        iconRes = R.drawable.ic_place_category_rock,
        categoryColorRes = R.color.colorPlaceCategoryRock,
    ),
    VIEWPOINT(
        osmQueryTags = listOf("tourism" to "viewpoint"),
        importantOsmTags = listOf(OsmTags.ELE, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = R.string.place_category_viewpoint.toMessage(),
        iconRes = R.drawable.ic_landscapes_telescope,
        categoryColorRes = R.color.colorPlaceCategoryViewpoint,
    ),
    CASTLE(
        osmQueryTags = listOf(
            "historic" to "castle",
            "historic" to "fortress",
            "tourism" to "castle",
            "castle" to null,
        ),
        importantOsmTags = listOf(
            OsmTags.DESCRIPTION,
            OsmTags.INSCRIPTION,
            OsmTags.OPENING_HOURS,
            OsmTags.FEE,
            OsmTags.WEBSITE,
            OsmTags.URL
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = R.string.place_category_castle.toMessage(),
        iconRes = R.drawable.ic_place_category_castle,
        categoryColorRes = R.color.colorPlaceCategoryCastle,
    ),
    HISTORIC(
        osmQueryTags = listOf(
            "historic" to "monument",
            "historic" to "memorial",
            "historic" to "ruins",
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.INSCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = R.string.place_category_historic.toMessage(),
        iconRes = R.drawable.ic_place_category_historic,
        categoryColorRes = R.color.colorPlaceCategoryHistoric,
    ),
    CHURCH(
        osmQueryTags = listOf(
            "amenity" to "place_of_worship",
            "building" to "church"
        ),
        importantOsmTags = listOf(
            OsmTags.DESCRIPTION,
            OsmTags.WEBSITE,
            OsmTags.URL,
            OsmTags.RELIGION,
            OsmTags.DENOMINATION
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = R.string.place_category_church.toMessage(),
        iconRes = R.drawable.ic_place_category_church,
        categoryColorRes = R.color.colorPlaceCategoryChurch,
    ),
    MUSEUM(
        osmQueryTags = listOf(
            "tourism" to "museum",
            "museum" to null,
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.INSCRIPTION, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = R.string.place_category_museum.toMessage(),
        iconRes = R.drawable.ic_place_category_museum,
        categoryColorRes = R.color.colorPlaceCategoryMuseum,
    ),
    SHOP(
        osmQueryTags = listOf(
            "shop" to "supermarket",
            "shop" to "convenience",
            "shop" to "greengrocer",
            "shop" to "deli",
            "shop" to "beverages",
            "shop" to "alcohol",
            "shop" to "general",
            "shop" to "organic",
            "shop" to "butcher",
            "shop" to "bakery",
        ),
        importantOsmTags = listOf(OsmTags.OPENING_HOURS, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.SHOPS,
        title = R.string.place_category_grocery.toMessage(),
        iconRes = R.drawable.ic_place_category_shop,
        categoryColorRes = R.color.colorPlaceCategoryShop,
    ),
    RESTAURANT(
        osmQueryTags = listOf(
            "amenity" to "restaurant",
            "amenity" to "fast_food",
            "amenity" to "cafe",
            "amenity" to "bar",
            "amenity" to "pub",
        ),
        importantOsmTags = listOf(OsmTags.OPENING_HOURS, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.SHOPS,
        title = R.string.place_category_restaurant.toMessage(),
        iconRes = R.drawable.ic_place_category_restaurant,
        categoryColorRes = R.color.colorPlaceCategoryRestaurant,
    ),
    PHARMACY(
        osmQueryTags = listOf("amenity" to "pharmacy"),
        importantOsmTags = listOf(OsmTags.OPENING_HOURS, OsmTags.WEBSITE, OsmTags.URL),
        categoryGroup = PlaceCategoryGroup.SHOPS,
        title = R.string.place_category_pharmacy.toMessage(),
        iconRes = R.drawable.ic_place_category_pharmacy,
        categoryColorRes = R.color.colorPlaceCategoryPharmacy,
    ),
    PARKING(
        osmQueryTags = listOf("amenity" to "parking"),
        importantOsmTags = listOf(OsmTags.OPERATOR, OsmTags.CAPACITY, OsmTags.FEE),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = R.string.place_category_parking.toMessage(),
        iconRes = R.drawable.ic_place_category_parking,
        categoryColorRes = R.color.colorPlaceCategoryParking,
    ),
    PUBLIC_TRANSPORT(
        osmQueryTags = listOf("public_transport" to null),
        importantOsmTags = listOf(OsmTags.OPERATOR, OsmTags.REF, OsmTags.ROUTE_REF),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = R.string.place_category_public_transport.toMessage(),
        iconRes = R.drawable.ic_place_category_public_transport,
        categoryColorRes = R.color.colorPlaceCategoryPublicTransport,
    ),
    DRINKING_WATER(
        osmQueryTags = listOf("amenity" to "drinking_water"),
        importantOsmTags = listOf(OsmTags.DESCRIPTION),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = R.string.place_category_drinking_water.toMessage(),
        iconRes = R.drawable.ic_place_category_drinking_water,
        categoryColorRes = R.color.colorPlaceCategoryDrinkingWater,
    ),
    FIREPLACE(
        osmQueryTags = listOf(
            "amenity" to "fireplace",
            "leisure" to "firepit",
            "amenity" to "bbq",
            "openfire" to null,
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = R.string.place_category_fireplace.toMessage(),
        iconRes = R.drawable.ic_place_category_fireplace,
        categoryColorRes = R.color.colorPlaceCategoryFireplace,
    ),
    CAMP_SITE(
        osmQueryTags = listOf(
            "tourism" to "camp_site",
            "tourism" to "picnic_site",
            "camp_site" to null,
        ),
        importantOsmTags = listOf(OsmTags.DESCRIPTION),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = R.string.place_category_camp_site.toMessage(),
        iconRes = R.drawable.ic_place_category_camp,
        categoryColorRes = R.color.colorPlaceCategoryCampSite,
    ),
    TOILET(
        osmQueryTags = listOf("amenity" to "toilets"),
        importantOsmTags = listOf(OsmTags.DESCRIPTION, OsmTags.FEE),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = R.string.place_category_toilets.toMessage(),
        iconRes = R.drawable.ic_place_category_toilets,
        categoryColorRes = R.color.colorPlaceCategoryToilet,
    ),
}
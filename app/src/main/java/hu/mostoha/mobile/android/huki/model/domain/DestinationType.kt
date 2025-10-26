package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R

enum class DestinationType {
    HIGHEST_PEAK,
    PEAK,
    VALLEY,
    LAKE,
    NATIONAL_PARK,
    CAVE,
    WATERFALL,
    CASTLE,
    ROCK,
    TOWN,
    PLATEAU,
    TRAIL,
    RIVER,
    LOOKOUT,
    ROCK_WITH_LOOKOUT,
    CHURCH,
    STATUE,
    WILDLIFE_PARK,
    MEMORIAL_PARK,
    ARBORETUM,
    MUSEUM,
    MEADOW,
    GORGE,
    OBSERVATORY,
    GARDEN,
    SPRING,
    FOREST,
    CAMP,
    ISLAND,
}

@DrawableRes
@Suppress("ComplexMethod")
fun DestinationType.resolveIcon(): Int {
    return when (this) {
        DestinationType.WATERFALL -> R.drawable.ic_place_category_waterfall
        DestinationType.RIVER -> R.drawable.ic_place_category_river
        DestinationType.SPRING -> R.drawable.ic_spring
        DestinationType.LAKE -> R.drawable.ic_lake
        DestinationType.PEAK -> R.drawable.ic_peak
        DestinationType.TOWN -> R.drawable.ic_place_category_city
        DestinationType.NATIONAL_PARK -> R.drawable.ic_place_category_forest
        DestinationType.CASTLE -> R.drawable.ic_landscapes_castle
        DestinationType.ROCK -> R.drawable.ic_place_category_rock
        DestinationType.VALLEY -> R.drawable.ic_valley
        DestinationType.GORGE -> R.drawable.ic_valley
        DestinationType.PLATEAU -> R.drawable.ic_plateau
        DestinationType.CAVE -> R.drawable.ic_place_category_cave
        DestinationType.HIGHEST_PEAK -> R.drawable.ic_place_category_peak
        DestinationType.TRAIL -> R.drawable.ic_okt_routes_chip
        DestinationType.ROCK_WITH_LOOKOUT -> R.drawable.ic_place_category_rock
        DestinationType.CHURCH -> R.drawable.ic_place_category_church
        DestinationType.STATUE -> R.drawable.ic_place_category_museum
        DestinationType.WILDLIFE_PARK -> R.drawable.ic_animal
        DestinationType.MEMORIAL_PARK -> R.drawable.ic_place_category_historic
        DestinationType.ARBORETUM -> R.drawable.ic_flower
        DestinationType.GARDEN -> R.drawable.ic_flower
        DestinationType.LOOKOUT -> R.drawable.ic_lookout
        DestinationType.MUSEUM -> R.drawable.ic_place_category_museum
        DestinationType.MEADOW -> R.drawable.ic_grass
        DestinationType.OBSERVATORY -> R.drawable.ic_landscapes_telescope
        DestinationType.FOREST -> R.drawable.ic_landscapes_forest
        DestinationType.CAMP -> R.drawable.ic_place_category_camp
        DestinationType.ISLAND -> R.drawable.ic_island
    }
}

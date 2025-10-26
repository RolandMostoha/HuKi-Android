package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R

enum class LandscapeType {
    MOUNTAIN_LOW,
    MOUNTAIN_MEDIUM,
    MOUNTAIN_HIGH,
    MOUNTAIN_WITH_LAKE,
    CAVE_SYSTEM,
    WINE_AREA,
    STAR_GAZING_AREA,
    MOUNTAIN_WITH_CASTLE,
    FOREST_AREA,
    PLAIN_LAND,
    LAKE,
}

@DrawableRes
fun LandscapeType.resolveIcon(): Int {
    return when (this) {
        LandscapeType.MOUNTAIN_LOW -> R.drawable.ic_landscapes_mountain_low
        LandscapeType.MOUNTAIN_MEDIUM -> R.drawable.ic_landscapes_mountain_medium
        LandscapeType.MOUNTAIN_HIGH -> R.drawable.ic_landscapes_mountain_high
        LandscapeType.MOUNTAIN_WITH_LAKE -> R.drawable.ic_landscapes_lake
        LandscapeType.MOUNTAIN_WITH_CASTLE -> R.drawable.ic_landscapes_castle
        LandscapeType.CAVE_SYSTEM -> R.drawable.ic_landscapes_cave
        LandscapeType.WINE_AREA -> R.drawable.ic_landscapes_grape
        LandscapeType.STAR_GAZING_AREA -> R.drawable.ic_landscapes_telescope
        LandscapeType.FOREST_AREA -> R.drawable.ic_landscapes_forest
        LandscapeType.PLAIN_LAND -> R.drawable.ic_landscapes_plain_land
        LandscapeType.LAKE -> R.drawable.ic_landscape_kayak
    }
}

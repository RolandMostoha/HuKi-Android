package hu.mostoha.mobile.android.huki.model.domain

data class Landscape(
    val id: String,
    val name: String,
    val type: LandscapeType
)

enum class LandscapeType {
    MOUNTAIN_RANGE_LOW,
    MOUNTAIN_RANGE_HIGH,
    PLATEAU_WITH_WATER,
    CAVE_SYSTEM
}

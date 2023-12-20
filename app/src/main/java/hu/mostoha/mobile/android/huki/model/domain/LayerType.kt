package hu.mostoha.mobile.android.huki.model.domain

enum class LayerType {
    MAPNIK,
    OPEN_TOPO,
    TUHU,
    HUNGARIAN_HIKING_LAYER,
    GPX
}

fun LayerType.isBase(): Boolean {
    return this == LayerType.MAPNIK || this == LayerType.OPEN_TOPO || this == LayerType.TUHU
}

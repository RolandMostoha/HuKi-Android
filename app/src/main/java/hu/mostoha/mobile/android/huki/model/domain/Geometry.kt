package hu.mostoha.mobile.android.huki.model.domain

import hu.mostoha.mobile.android.huki.util.sparsify

sealed class Geometry(open val osmId: String) {

    data class Node(
        override val osmId: String,
        val location: Location
    ) : Geometry(osmId)

    data class Way(
        override val osmId: String,
        val locations: List<Location>,
        val distance: Int
    ) : Geometry(osmId)

    data class Relation(
        override val osmId: String,
        val ways: List<Way>
    ) : Geometry(osmId)

}

fun Geometry.pointsCount(): Int {
    return when (this) {
        is Geometry.Node -> 1
        is Geometry.Way -> locations.size
        is Geometry.Relation -> ways.sumOf { it.locations.size }
    }
}

fun Geometry.sparsify(minDistance: Int): Geometry {
    return when (this) {
        is Geometry.Way -> {
            Geometry.Way(
                osmId = this.osmId,
                locations = this.locations.sparsify(minDistance),
                distance = this.distance
            )
        }
        is Geometry.Relation -> {
            Geometry.Relation(
                osmId = this.osmId,
                ways = this.ways.map { way ->
                    Geometry.Way(
                        osmId = way.osmId,
                        locations = way.locations.sparsify(minDistance),
                        distance = way.distance
                    )
                }
            )
        }
        else -> this
    }
}

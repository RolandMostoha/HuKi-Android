package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.pointsCount
import hu.mostoha.mobile.android.huki.model.domain.sparsify
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDetailsNetworkDomainMapper
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.util.distanceBetween
import timber.log.Timber
import java.io.InputStreamReader
import javax.inject.Inject

class DefaultLandscapeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi,
    private val placeDetailsMapper: PlaceDetailsNetworkDomainMapper
) : LandscapeRepository {

    companion object {
        private const val SPARSIFY_DISTANCE_M = 1500
        private val MULTIPOLYGON_LANDSCAPES = listOf(
            R.string.landscape_alfold,
            R.string.landscape_orseg,
        )
    }

    override suspend fun getLandscapes(location: Location?): List<Landscape> {
        return if (location == null) {
            LOCAL_LANDSCAPES.sortedBy { context.getString(it.nameRes) }
        } else {
            LOCAL_LANDSCAPES.sortedBy { location.distanceBetween(it.center) }
        }
    }

    override suspend fun getLandscapeGeometryList(): List<Pair<Landscape, Geometry>> {
        val inputStream = context.resources.openRawResource(R.raw.landscapes_overpass_response)
        val json = InputStreamReader(inputStream).use { it.readText() }
        val adapter = moshi.adapter(OverpassQueryResponse::class.java)
        val response = adapter.fromJson(json)

        checkNotNull(response) { "Landscapes: parse failure" }

        val geometryList = placeDetailsMapper.mapGeometryList(response)

        return getLandscapes().map { landscape ->
            val landscapeName = context.getString(landscape.nameRes)
            val geometry = geometryList.first { it.osmId == landscape.osmId }
            val sparsified = geometry.sparsify(SPARSIFY_DISTANCE_M)

            Timber.d("$landscapeName sparsified from ${geometry.pointsCount()} to ${sparsified.pointsCount()}")

            val resultGeometry = when {
                MULTIPOLYGON_LANDSCAPES.contains(landscape.nameRes) -> {
                    val allPoints = (sparsified as Geometry.Relation)
                        .ways
                        .flatMap { it.locations }
                        .asReversed()
                        .distinct()
                        .asReversed()

                    Geometry.Way(geometry.osmId, allPoints, 0)
                }
                else -> {
                    sparsified
                }
            }

            landscape to resultGeometry
        }
    }

}

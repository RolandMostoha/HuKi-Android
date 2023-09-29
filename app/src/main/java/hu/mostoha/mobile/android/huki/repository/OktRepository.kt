package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import io.ticofab.androidgpxparser.parser.GPXParser
import javax.inject.Inject

class OktRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val layersDomainModelMapper: LayersDomainModelMapper,
) {

    suspend fun getOktFullRoute(): List<Location> {
        val inputStream = context.resources.openRawResource(R.raw.okt_full)

        val gpx = GPXParser().parse(inputStream)

        val gpxDetails = layersDomainModelMapper.mapGpxDetails("okt_complete", gpx)

        return gpxDetails.locations
    }

}

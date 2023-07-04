package hu.mostoha.mobile.android.huki.repository

import android.net.Uri
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange

interface LayersRepository {

    suspend fun getHikingLayerZoomRanges(): List<TileZoomRange>

    suspend fun getGpxDetails(fileUri: Uri?): GpxDetails

    suspend fun getRoutePlannerGpxDetails(fileUri: Uri): GpxDetails

    suspend fun getGpxHistory(): GpxHistory

}

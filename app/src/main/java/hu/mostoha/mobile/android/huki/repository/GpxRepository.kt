package hu.mostoha.mobile.android.huki.repository

import android.net.Uri
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem

interface GpxRepository {

    suspend fun getGpxDetails(fileUri: Uri?): GpxDetails

    suspend fun getRoutePlannerGpxDetails(fileUri: Uri): GpxDetails

    suspend fun getGpxHistory(): GpxHistory

    suspend fun getRecentGpxHistory(): List<GpxHistoryItem>

    suspend fun deleteGpx(fileUri: Uri)

    suspend fun renameGpx(fileUri: Uri, newName: String)

}

package hu.mostoha.mobile.android.huki.repository

import java.io.File

interface HikingLayerRepository {

    suspend fun getHikingLayerFile(): File?

    suspend fun downloadHikingLayerFile(): Long

    suspend fun saveHikingLayerFile(downloadId: Long)

}
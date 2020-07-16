package hu.mostoha.mobile.android.turistautak.repository

import java.io.File

interface LayerRepository {
    suspend fun getHikingLayerFile(): File?
    suspend fun downloadHikingLayerFile(): Long
    suspend fun saveHikingLayerFile(downloadId: Long)
}
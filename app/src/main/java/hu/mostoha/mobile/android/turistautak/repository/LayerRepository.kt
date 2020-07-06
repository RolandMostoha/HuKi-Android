package hu.mostoha.mobile.android.turistautak.repository

import java.io.File

interface LayerRepository {
    fun getHikingLayerFile(): File?
    fun downloadHikingLayerFile(): Long
    fun saveHikingLayerFile(downloadId: Long)
}
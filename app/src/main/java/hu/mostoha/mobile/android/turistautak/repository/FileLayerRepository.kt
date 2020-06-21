package hu.mostoha.mobile.android.turistautak.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.configuration.OsmConfiguration
import java.io.File
import javax.inject.Inject

class FileLayerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : LayerRepository {

    override fun getHikingLayerFile(): File? {
        val basePath = OsmConfiguration.getOsmDroidBasePath(context).path
        val file = File(basePath + OsmConfiguration.FILE_PATH_HIKING_LAYER)
        if (file.exists()) {
            return file
        }
        return null
    }

}
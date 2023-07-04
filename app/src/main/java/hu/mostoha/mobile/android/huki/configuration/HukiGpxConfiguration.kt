package hu.mostoha.mobile.android.huki.configuration

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.extensions.getOrCreateDirectory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HukiGpxConfiguration @Inject constructor(
    @ApplicationContext private val context: Context,
) : GpxConfiguration {

    companion object {
        private const val DIRECTORY_NAME_GPX = "gpx"
        private const val DIRECTORY_NAME_ROUTE_PLANNER = "routeplanner"
        private const val DIRECTORY_NAME_EXTERNAL = "external"
    }

    override fun getRoutePlannerGpxDirectory(): String {
        val baseDir = getOrCreateDirectory(
            parent = context.filesDir.path,
            child = DIRECTORY_NAME_GPX,
        ) ?: error("Could not create GPX directory")

        val routePlannerDir = getOrCreateDirectory(
            parent = baseDir.path,
            child = DIRECTORY_NAME_ROUTE_PLANNER,
        ) ?: error("Could not create route planner directory")

        return routePlannerDir.path
    }

    override fun getExternalGpxDirectory(): String {
        val baseDir = getOrCreateDirectory(
            parent = context.filesDir.path,
            child = DIRECTORY_NAME_GPX,
        ) ?: error("Could not create GPX directory")

        val externalDir = getOrCreateDirectory(
            parent = baseDir.path,
            child = DIRECTORY_NAME_EXTERNAL,
        ) ?: error("Could not create external GPX directory")

        return externalDir.path
    }

    override fun clearAllGpxFiles() {
        File(getRoutePlannerGpxDirectory())
            .listFiles()
            ?.forEach { it.delete() }
        File(getExternalGpxDirectory())
            .listFiles()
            ?.forEach { it.delete() }
    }

}

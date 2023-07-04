package hu.mostoha.mobile.android.huki.configuration

interface GpxConfiguration {

    fun getRoutePlannerGpxDirectory(): String

    fun getExternalGpxDirectory(): String

    fun clearAllGpxFiles()

}

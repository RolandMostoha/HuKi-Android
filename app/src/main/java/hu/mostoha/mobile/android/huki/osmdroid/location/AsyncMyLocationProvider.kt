package hu.mostoha.mobile.android.huki.osmdroid.location

import android.location.Location
import kotlinx.coroutines.flow.Flow
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

/**
 * Extension interface to use [FusedLocationProvider] in OSMDroid overlays.
 */
interface AsyncMyLocationProvider : IMyLocationProvider {

    override fun getLastKnownLocation(): Location? = null

    /**
     * Returns the monitored [Location]s in a [Flow] stream starting with the last known location.
     */
    fun getLocationFlow(): Flow<Location>

    /**
     * Returns the last known location via suspending function.
     */
    suspend fun getLastKnownLocationCoroutine(): Location?

}

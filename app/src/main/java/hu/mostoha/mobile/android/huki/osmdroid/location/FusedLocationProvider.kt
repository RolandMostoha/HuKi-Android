package hu.mostoha.mobile.android.huki.osmdroid.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.util.MY_LOCATION_MIN_INTERVAL_TIME_MS
import hu.mostoha.mobile.android.huki.util.MY_LOCATION_TIME_MS
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * OSMDroid's [IMyLocationProvider] implementation that uses Google's Fused Location Provider
 * and provides the location updates via coroutines and [Flow].
 */
class FusedLocationProvider @Inject constructor(
    @ApplicationContext val context: Context
) : AsyncMyLocationProvider {

    private var myLocationConsumer: IMyLocationConsumer? = null

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var fusedLocationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    private val _locationFlow = callbackFlow {
        Timber.d("Requesting location updates")

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation ?: return

                myLocationConsumer?.onLocationChanged(lastLocation, this@FusedLocationProvider)

                trySend(lastLocation)

                Timber.d("Location update: $lastLocation")
            }
        }

        val locationRequest = LocationRequest.Builder(MY_LOCATION_TIME_MS)
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(MY_LOCATION_MIN_INTERVAL_TIME_MS)
            .setWaitForAccurateLocation(true)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            .addOnSuccessListener {
                Timber.d("Starting location updates")
                fusedLocationCallback = locationCallback
            }
            .addOnFailureListener { exception ->
                Timber.d(exception, "Failure on requesting location updates")
                close(exception)
            }

        awaitClose {
            Timber.d("Location Flow stream is closed, stopping location monitoring")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun startLocationProvider(myLocationConsumer: IMyLocationConsumer): Boolean {
        this.myLocationConsumer = myLocationConsumer

        return true
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocationCoroutine(): Location? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { exception ->
                    Timber.w(exception, "Exception during get last known location coroutine: ${exception.message}")
                    continuation.resume(null)
                }
        }
    }

    override fun getLocationFlow(): Flow<Location> = _locationFlow

    override fun stopLocationProvider() {
        Timber.d("Stopping location monitoring")
        fusedLocationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        fusedLocationCallback = null
        myLocationConsumer = null
    }

    override fun destroy() {
        Timber.d("Destroying location provider, stopping location monitoring")
        fusedLocationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        fusedLocationCallback = null
        myLocationConsumer = null
    }

}

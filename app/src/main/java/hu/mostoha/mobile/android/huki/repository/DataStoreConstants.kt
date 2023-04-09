package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.preferences.core.floatPreferencesKey

object DataStoreConstants {

    const val USER_PREFERENCES = "user_preferences"

    object Settings {
        val MAP_SCALE_FACTOR = floatPreferencesKey("map_scale_factor")
    }

}

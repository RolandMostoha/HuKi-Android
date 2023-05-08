package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.preferences.core.doublePreferencesKey

object DataStoreConstants {

    const val USER_PREFERENCES = "user_preferences"

    object Settings {
        val MAP_SCALE_FACTOR = doublePreferencesKey("settings_map_scale_factor")
    }

}

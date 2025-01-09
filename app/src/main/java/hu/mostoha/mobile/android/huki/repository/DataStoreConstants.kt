package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreConstants {

    const val USER_PREFERENCES = "user_preferences"

    object Settings {
        val BASE_LAYER = stringPreferencesKey("settings_base_layer")
        val THEME = intPreferencesKey("settings_theme")
        val MAP_SCALE_FACTOR = doublePreferencesKey("settings_map_scale_factor")
    }

    object NewFeatures {
        val NEW_FEATURES_SEEN_VERSION = stringPreferencesKey("new_features_seen_version")
    }

}

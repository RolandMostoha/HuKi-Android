package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreConstants {

    const val USER_PREFERENCES = "user_preferences"

    object Settings {
        val THEME = intPreferencesKey("settings_theme")
        val MAP_SCALE_FACTOR = doublePreferencesKey("settings_map_scale_factor")
        val HIKE_RECOMMENDER_INFO_ENABLED = booleanPreferencesKey("settings_hike_recommender_info_enabled")

    }

    object NewFeatures {
        val NEW_FEATURES_SEEN_VERSION = stringPreferencesKey("new_features_seen_version")
    }

}

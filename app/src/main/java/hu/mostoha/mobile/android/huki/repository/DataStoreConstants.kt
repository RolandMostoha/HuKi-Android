package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object DataStoreConstants {

    const val USER_PREFERENCES = "user_preferences"

    object Settings {
        val MAP_SCALE_FACTOR = doublePreferencesKey("settings_map_scale_factor")
        val THEME = intPreferencesKey("settings_theme")
        val HIKE_RECOMMENDER_INFO_ENABLED = booleanPreferencesKey("settings_hike_recommender_info_enabled")
    }

}

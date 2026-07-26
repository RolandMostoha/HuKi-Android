package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object DataStoreConstants {

    const val USER_PREFERENCES = "user_preferences"

    object MapConfig {
        val BOUNDING_BOX_NORTH = doublePreferencesKey("map_config_bounding_box_north")
        val BOUNDING_BOX_EAST = doublePreferencesKey("map_config_bounding_box_east")
        val BOUNDING_BOX_SOUTH = doublePreferencesKey("map_config_bounding_box_south")
        val BOUNDING_BOX_WEST = doublePreferencesKey("map_config_bounding_box_west")
    }

    object Settings {
        val BASE_LAYER = stringPreferencesKey("settings_base_layer")
        val THEME = intPreferencesKey("settings_theme")
        val MAP_SCALE_FACTOR = doublePreferencesKey("settings_map_scale_factor")
        val GPX_SLOPE_COLORING_ENABLED = booleanPreferencesKey("gpx_slope_coloring_enabled")
    }

    object NewFeatures {
        val NEW_FEATURES_SEEN_VERSION = stringPreferencesKey("new_features_seen_version")
    }

    object Support {
        val PURCHASED_ONE_TIME_PRODUCTS = stringSetPreferencesKey("support_purchased_one_time_products")
        val RECORDED_PURCHASE_TOKENS = stringSetPreferencesKey("support_recorded_purchase_tokens")
        val LEGACY_HISTORY_MIGRATED = booleanPreferencesKey("support_legacy_history_migrated")
    }

}

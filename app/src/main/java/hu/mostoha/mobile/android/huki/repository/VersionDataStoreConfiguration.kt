package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.extensions.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class VersionDataStoreConfiguration @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : VersionConfiguration {

    companion object {
        private const val WHATS_NEW_LATEST_DIRECTORY = "latest"
        private const val WHATS_NEW_EN_TAG_MATCHER = "en"
        private const val WHATS_NEW_HU_TAG_MATCHER = "hu"
        private val LOCALE_HU = Locale.forLanguageTag("hu-HU")
    }

    override fun getNewFeatures(versionName: String): Flow<String?> {
        return dataStore.data
            .map { preferences ->
                val lastSeenVersion = preferences[DataStoreConstants.NewFeatures.NEW_FEATURES_SEEN_VERSION]
                val latestVersion = BuildConfig.VERSION_NAME

                val locale = Locale.getDefault()
                val localeLanguageTag = locale.toLanguageTag()
                val languageTag = when {
                    localeLanguageTag.contains(WHATS_NEW_EN_TAG_MATCHER) -> Locale.US.toLanguageTag()
                    localeLanguageTag.contains(WHATS_NEW_HU_TAG_MATCHER) -> LOCALE_HU.toLanguageTag()
                    else -> Locale.US.toLanguageTag()
                }

                if (lastSeenVersion != versionName) {
                    val version = if (versionName == latestVersion) {
                        WHATS_NEW_LATEST_DIRECTORY
                    } else {
                        versionName
                    }

                    context.readText("whatsnew/$version/whatsnew-$languageTag")
                } else {
                    null
                }
            }
    }

    override suspend fun saveNewFeaturesSeen(versionName: String) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.NewFeatures.NEW_FEATURES_SEEN_VERSION] = versionName
        }
    }

}

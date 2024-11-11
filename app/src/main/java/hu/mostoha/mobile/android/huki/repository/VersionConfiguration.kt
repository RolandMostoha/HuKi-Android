package hu.mostoha.mobile.android.huki.repository

import kotlinx.coroutines.flow.Flow

interface VersionConfiguration {

    fun getNewFeatures(versionName: String): Flow<String?>

    suspend fun saveNewFeaturesSeen(versionName: String)

}

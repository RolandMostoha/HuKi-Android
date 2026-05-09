package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.NewFeatures
import kotlinx.coroutines.flow.Flow

interface VersionConfiguration {

    fun getNewFeatures(versionName: String): Flow<NewFeatures?>

    suspend fun saveNewFeaturesSeen(versionName: String)

}

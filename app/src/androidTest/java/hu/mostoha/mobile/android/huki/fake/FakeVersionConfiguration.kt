package hu.mostoha.mobile.android.huki.fake

import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeVersionConfiguration @Inject constructor() : VersionConfiguration {

    override fun getNewFeatures(versionName: String): Flow<String?> = flowOf(null)

    override suspend fun saveNewFeaturesSeen(versionName: String) = Unit

}

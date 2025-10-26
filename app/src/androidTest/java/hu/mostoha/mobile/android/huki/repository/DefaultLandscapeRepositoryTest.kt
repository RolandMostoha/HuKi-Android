package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class DefaultLandscapeRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: DefaultLandscapeRepository

    @Test
    fun givenNullLocation_whenGetLandscapes_thenLandscapesReturnSorted() {
        runTest {
            val landscapes = repository.getLandscapes(null)

            assertThat(landscapes.first().nameRes).isEqualTo(R.string.landscape_aggteleki_karszt)
            assertThat(landscapes.last().nameRes).isEqualTo(R.string.landscape_orseg)
        }
    }

    @Test
    fun givenLocation_whenGetLandscapes_thenLandscapesReturnSorted() {
        runTest {
            val landscapes = repository.getLandscapes(DEFAULT_LANDSCAPE.center)

            assertThat(landscapes.first().nameRes).isEqualTo(DEFAULT_LANDSCAPE.nameRes)
        }
    }

    @Test
    fun whenGetLandscapeGeometryList_thenLandscapesReturn() {
        runTest {
            val landscapeGeometryList = repository.getLandscapeGeometryList()

            assertThat(landscapeGeometryList).isNotEmpty()
        }
    }

    companion object {
        private val DEFAULT_LANDSCAPE = LOCAL_LANDSCAPES[1]
    }

}

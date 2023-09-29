package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class OktRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: OktRepository

    @Test
    fun givenLocalOktRoutes_whenGetOktFullRoute_thenStartAndEndPositionsArePresent() = runTest {
        val locations = repository.getOktFullRoute()

        LOCAL_OKT_ROUTES.map { it.start to it.end }.forEach { (start, end) ->
            assertThat(locations).contains(start)
            assertThat(locations).contains(end)
        }
    }

}

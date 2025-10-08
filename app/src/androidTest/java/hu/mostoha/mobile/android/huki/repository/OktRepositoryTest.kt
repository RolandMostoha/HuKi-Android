package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.model.domain.OktType
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
    fun givenLocalOktRoutes_whenGetOktRoutes_thenStartAndEndPositionsArePresent() = runTest {
        val oktRoutes = repository.getOktRoutes(OktType.OKT)

        assertThat(oktRoutes.oktRoutes).hasSize(28)
        assertThat(oktRoutes.stampWaypoints).isNotEmpty()
    }

    @Test
    fun givenLocalRpddkRoutes_whenGetOktRoutes_thenStartAndEndPositionsArePresent() = runTest {
        val oktRoutes = repository.getOktRoutes(OktType.RPDDK)

        assertThat(oktRoutes.oktRoutes).hasSize(12)
        assertThat(oktRoutes.stampWaypoints).isNotEmpty()
    }

    @Test
    fun givenLocalAktRoutes_whenGetOktRoutes_thenStartAndEndPositionsArePresent() = runTest {
        val oktRoutes = repository.getOktRoutes(OktType.AKT)

        assertThat(oktRoutes.oktRoutes).hasSize(14)
        assertThat(oktRoutes.stampWaypoints).isNotEmpty()
    }

}

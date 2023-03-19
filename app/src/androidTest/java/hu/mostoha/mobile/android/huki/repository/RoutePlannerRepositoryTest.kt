package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE
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
class RoutePlannerRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: RoutePlannerRepository

    @Test
    fun whenGetRouteResponse_thenResultIsNotNull() = runTest {
        val waypoints = listOf(DEFAULT_WAYPOINT_1, DEFAULT_WAYPOINT_2)

        val routeResponse = repository.getRoutePlan(waypoints)

        assertThat(routeResponse).isNotNull()
    }

    companion object {
        val DEFAULT_WAYPOINT_1 = Location(
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
        )
        val DEFAULT_WAYPOINT_2 = Location(
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
        )
    }

}

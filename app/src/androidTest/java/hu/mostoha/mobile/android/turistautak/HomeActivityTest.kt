package hu.mostoha.mobile.android.turistautak

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeActivityTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<HomeActivity>()

    @Test
    fun whenScreenStarts_thenMapShouldShown() {
        onView(withId(R.id.homeMapView)).check(matches(isDisplayed()))
    }

}

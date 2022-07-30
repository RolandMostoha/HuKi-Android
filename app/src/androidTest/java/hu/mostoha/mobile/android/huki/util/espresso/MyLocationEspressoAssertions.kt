package hu.mostoha.mobile.android.huki.util.espresso

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import org.hamcrest.Description
import org.osmdroid.views.MapView

fun @receiver:IdRes Int.isFollowLocationEnabled(isFollowLocationEnabled: Boolean) {
    onView(withId(this)).check(matches(isFollowLocationEnabledMatcher(isFollowLocationEnabled)))
}

fun isFollowLocationEnabledMatcher(isFollowLocationEnabled: Boolean): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            val overlay = mapView.overlays
                .filterIsInstance<MyLocationOverlay>()
                .first()

            return overlay.isMyLocationEnabled && overlay.isFollowLocationEnabled
        }

        override fun describeTo(description: Description) {
            description.appendText("Follow locations is enabled: $isFollowLocationEnabled")
        }
    }
}

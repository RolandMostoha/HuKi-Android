package hu.mostoha.mobile.android.huki.util.espresso

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

inline fun <reified T> @receiver:IdRes Int.hasOverlayInPosition(position: Int) {
    onView(withId(this)).check(matches(hasOverlayMatcher<T>(position)))
}

inline fun <reified T> @receiver:IdRes Int.hasNotOverlayInPosition(position: Int) {
    onView(withId(this)).check(matches(not(hasOverlayMatcher<T>(position))))
}

inline fun <reified T> hasOverlayMatcher(position: Int): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            val overlay = mapView.overlays.getOrNull(position)

            return overlay != null && overlay is T
        }

        override fun describeTo(description: Description) {
            description.appendText("Has overlay with position $position")
        }
    }
}

fun @receiver:IdRes Int.hasCenterAndZoom(center: GeoPoint, zoom: Double) {
    onView(withId(this)).check(matches(hasCenterAndZoomMatcher(center, zoom)))
}

fun hasCenterAndZoomMatcher(center: GeoPoint, zoom: Double): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            val actualCenter = mapView.mapCenter
            val actualZoom = mapView.zoomLevelDouble

            return center == actualCenter && zoom == actualZoom
        }

        override fun describeTo(description: Description) {
            description.appendText("Has center and zoom $center, $zoom")
        }
    }
}

fun @receiver:IdRes Int.zoomTo(zoomLevel: Double) {
    onView(withId(this)).perform(zoomToAction(zoomLevel))
}

fun zoomToAction(zoomLevel: Double): ViewAction {
    return object : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(MapView::class.java)
        }

        override fun perform(uiController: UiController, view: View) {
            val mapView = view as MapView

            mapView.controller.zoomTo(zoomLevel)
        }

        override fun getDescription(): String {
            return "Sets the zoom level of MapView"
        }
    }
}

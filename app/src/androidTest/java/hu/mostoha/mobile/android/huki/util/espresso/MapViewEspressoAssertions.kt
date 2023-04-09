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
import com.google.android.material.slider.Slider
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.abs

inline fun <reified T : Overlay> @receiver:IdRes Int.hasOverlay() {
    onView(withId(this)).check(matches(hasOverlayMatcher<T>()))
}

inline fun <reified T : Overlay> @receiver:IdRes Int.hasNoOverlay() {
    onView(withId(this)).check(matches(not(hasOverlayMatcher<T>())))
}

inline fun <reified T : Overlay> hasOverlayMatcher(): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            val overlays = mapView.overlays.filterIsInstance<T>()

            return overlays.isNotEmpty() && overlays.all { it.isEnabled }
        }

        override fun describeTo(description: Description) {
            description.appendText("Has visible overlay with type ${T::class.java}")
        }

    }
}

inline fun <reified T : Overlay> @receiver:IdRes Int.hasInvisibleOverlay() {
    onView(withId(this)).check(matches(hasInvisibleOverlayMatcher<T>()))
}

inline fun <reified T : Overlay> hasInvisibleOverlayMatcher(): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            val overlays = mapView.overlays.filterIsInstance<T>()

            return overlays.isNotEmpty() && overlays.all { !it.isEnabled }
        }

        override fun describeTo(description: Description) {
            description.appendText("Has invisible overlay with type ${T::class.java}")
        }

    }
}

inline fun <reified T : Overlay> @receiver:IdRes Int.hasOverlayCount(count: Int) {
    onView(withId(this)).check(matches(hasOverlayCountMatcher<T>(count)))
}

inline fun <reified T : Overlay> hasOverlayCountMatcher(count: Int): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            return mapView.overlays
                .filterIsInstance<T>()
                .size == count
        }

        override fun describeTo(description: Description) {
            description.appendText("Has $count overlay(s) with type ${T::class.java}")
        }

    }
}

fun @receiver:IdRes Int.hasOverlaysInOrder(comparator: Comparator<Overlay>) {
    onView(withId(this))
        .check(
            matches(object : BoundedMatcher<View, MapView>(MapView::class.java) {
                override fun matchesSafely(mapView: MapView?): Boolean {
                    if (mapView == null) return false

                    return mapView.overlays == mapView.overlays.sortedWith(comparator)

                }

                override fun describeTo(description: Description) {
                    description.appendText("Has overlays in order ${comparator::class.java}")
                }
            })
        )
}

fun @receiver:IdRes Int.hasBaseTileSource(tileSource: ITileSource) {
    onView(withId(this)).check(matches(hasBaseTileSourceMatcher(tileSource)))
}

private fun hasBaseTileSourceMatcher(tileSource: ITileSource): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            return mapView.tileProvider.tileSource == tileSource
        }

        override fun describeTo(description: Description) {
            description.appendText("Has base tile source: $tileSource")
        }

    }
}

fun @receiver:IdRes Int.hasCenterAndZoom(center: GeoPoint, zoom: Double) {
    onView(withId(this)).check(matches(hasCenterAndZoomMatcher(center, zoom)))
}

private fun hasCenterAndZoomMatcher(center: GeoPoint, zoom: Double): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            val actualCenter = mapView.mapCenter
            val actualZoom = mapView.zoomLevelDouble

            return center.latitude.equalsDelta(actualCenter.latitude) &&
                center.longitude.equalsDelta(actualCenter.longitude) &&
                zoom.toInt() == actualZoom.toInt()
        }

        override fun describeTo(description: Description) {
            description.appendText("Failed: has center: $center and zoom: $zoom")
        }

        private fun Double.equalsDelta(other: Double) = abs(this - other) < 0.000001

    }
}

fun @receiver:IdRes Int.zoomTo(zoomLevel: Double) {
    onView(withId(this)).perform(zoomToAction(zoomLevel))
}

private fun zoomToAction(zoomLevel: Double): ViewAction {
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

fun @receiver:IdRes Int.hasTileScaleFactor(tileScaleFactor: Float) {
    onView(withId(this)).check(matches(hasTileScaleFactorMatcher(tileScaleFactor)))
}

private fun hasTileScaleFactorMatcher(tileScaleFactor: Float): BoundedMatcher<View, MapView> {
    return object : BoundedMatcher<View, MapView>(MapView::class.java) {

        override fun matchesSafely(mapView: MapView?): Boolean {
            if (mapView == null) return false

            return mapView.tilesScaleFactor == tileScaleFactor
        }

        override fun describeTo(description: Description) {
            description.appendText("Failed: has tileScaleFactor: $tileScaleFactor")
        }

    }
}

fun @receiver:IdRes Int.setSliderValue(sliderValue: Float) {
    onView(withId(this)).perform(setSliderValueAction(sliderValue))
}

private fun setSliderValueAction(sliderValue: Float): ViewAction {
    return object : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(Slider::class.java)
        }

        override fun perform(uiController: UiController, view: View) {
            val slider = view as Slider

            slider.value = sliderValue
        }

        override fun getDescription(): String {
            return "Sets the zoom level of MapView"
        }

    }
}

fun @receiver:IdRes Int.hasSliderValue(sliderValue: Float) {
    onView(withId(this)).check(matches(hasSliderValueMatcher(sliderValue)))
}

private fun hasSliderValueMatcher(sliderValue: Float): BoundedMatcher<View, Slider> {
    return object : BoundedMatcher<View, Slider>(Slider::class.java) {

        override fun matchesSafely(slider: Slider?): Boolean {
            if (slider == null) return false

            return slider.value == sliderValue
        }

        override fun describeTo(description: Description) {
            description.appendText("Failed: has sliderValue: $sliderValue")
        }

    }
}
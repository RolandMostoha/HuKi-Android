package hu.mostoha.mobile.android.huki.extensions

import org.hamcrest.CoreMatchers
import org.junit.Assert

inline fun <reified T> assertWithType(expected: Any, actual: Any?, data: (T) -> Any) {
    Assert.assertThat(actual, CoreMatchers.instanceOf(T::class.java))
    Assert.assertEquals(expected, data.invoke(actual as T))
}
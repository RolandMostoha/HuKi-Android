package hu.mostoha.mobile.android.huki.deeplink

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.DeeplinkEvent
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_LANDSCAPE_OSM_ID
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class DeeplinkHandlerTest {

    private lateinit var deeplinkHandler: DeeplinkHandler

    @Before
    fun setUp() {
        deeplinkHandler = DeeplinkHandler()
    }

    @Test
    fun givenIntentWithoutAction_whenHandleDeeplink_thenNullReturned() {
        val intent = Intent()

        val event = deeplinkHandler.handleDeeplink(intent)

        assertThat(event).isNull()
    }

    @Test
    fun givenIntentWithoutData_whenHandleDeeplink_thenNullReturned() {
        val intent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
        }

        val event = deeplinkHandler.handleDeeplink(intent)

        assertThat(event).isNull()
    }

    @Test
    fun givenIntentWithWrongHost_whenHandleDeeplink_thenNullReturned() {
        val intent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setData(Uri.parse("MALFORMED_DEEPLINK"))
        }

        val event = deeplinkHandler.handleDeeplink(intent)

        assertThat(event).isNull()
    }

    @Test
    fun givenPlaceDeeplink_whenHandleDeeplink_thenPlaceDetailsEventReturned() {
        val lat = 47.497913
        val lon = 19.1234
        val intent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setData(Uri.parse(DEEPLINK_PLACE.format(lat, lon)))
        }

        val event = deeplinkHandler.handleDeeplink(intent)

        assertThat(event).isEqualTo(DeeplinkEvent.PlaceDetails(lat, lon))
    }

    @Test
    fun givenLandscapeDeeplink_whenHandleDeeplink_thenLandscapeDetailsEventReturned() {
        val intent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
            setData(Uri.parse(DEEPLINK_LANDSCAPE.format(DEFAULT_LANDSCAPE_OSM_ID)))
        }

        val event = deeplinkHandler.handleDeeplink(intent)

        assertThat(event).isEqualTo(DeeplinkEvent.LandscapeDetails(DEFAULT_LANDSCAPE_OSM_ID))
    }

    companion object {
        private const val DEEPLINK_PLACE = "https://huki.hu/place?lat=%s&lon=%s"
        private const val DEEPLINK_LANDSCAPE = "http://huki.hu/landscape?osmId=%s"
    }

}

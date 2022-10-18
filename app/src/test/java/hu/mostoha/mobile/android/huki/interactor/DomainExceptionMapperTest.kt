package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.GatewayTimeoutException
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.interactor.exception.TooManyRequestsException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.xmlpull.v1.XmlPullParserException
import retrofit2.HttpException
import retrofit2.Response
import java.io.FileNotFoundException

class DomainExceptionMapperTest {

    @Test
    fun `Given HTTP 429 Too Many Requests exception, when map, then TooManyRequestsException returns`() {
        val exception = HttpException(Response.error<Unit>(429, "".toResponseBody()))

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(TooManyRequestsException(exception))
        assertThat(mappedException.messageRes).isEqualTo(R.string.error_message_too_many_requests.toMessage())
    }

    @Test
    fun `Given HTTP 504 Gateway Timeout exception, when map, then GatewayTimeoutException returns`() {
        val exception = HttpException(Response.error<Unit>(504, "".toResponseBody()))

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(GatewayTimeoutException(exception))
        assertThat(mappedException.messageRes).isEqualTo(R.string.error_message_gateway_timeout.toMessage())
    }

    @Test
    fun `Given CancellationException, when map, then CancellationException returns`() {
        val exception = CancellationException("StandaloneCoroutine was cancelled")

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(JobCancellationException(exception))
        assertThat(mappedException.messageRes).isEqualTo(R.string.error_message_unknown.toMessage())
    }

    @Test
    fun `Given FileNotFoundException, when map, then GpxParseFailedException returns`() {
        val exception = FileNotFoundException("File not found for URI")

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(GpxParseFailedException(exception))
        assertThat(mappedException.messageRes).isEqualTo(R.string.error_message_gpx_parse_failed.toMessage())
    }

    @Test
    fun `Given XmlPullParserException, when map, then GpxParseFailedException returns`() {
        val exception = XmlPullParserException("Unexpected token")

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(GpxParseFailedException(exception))
        assertThat(mappedException.messageRes).isEqualTo(R.string.error_message_gpx_parse_failed.toMessage())
    }

    @Test
    fun `Given IllegalStateException, when map, then UnknownException returns`() {
        val exception = IllegalStateException("Unknown error")

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(UnknownException(exception))
        assertThat(mappedException.messageRes).isEqualTo(R.string.error_message_unknown.toMessage())
    }

}

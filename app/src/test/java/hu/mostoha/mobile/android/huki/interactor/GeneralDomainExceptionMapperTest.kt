package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GeneralDomainExceptionMapperTest {

    @Test
    fun `Given IllegalStateException, when map, then domain exception returns with unknown message`() {
        val exception = IllegalStateException("Unknown error")

        val mappedException = GeneralDomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(
            DomainException(
                throwable = exception,
                messageRes = R.string.error_message_unknown.toMessage()
            )
        )
    }

    @Test
    fun `Given HTTP 429 Too Many Requests exception, when map, then domain exception returns with correct message`() {
        val exception = HttpException(Response.error<Unit>(429, "".toResponseBody()))

        val mappedException = GeneralDomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(
            DomainException(
                throwable = exception,
                messageRes = R.string.error_message_too_many_requests.toMessage()
            )
        )
    }

}

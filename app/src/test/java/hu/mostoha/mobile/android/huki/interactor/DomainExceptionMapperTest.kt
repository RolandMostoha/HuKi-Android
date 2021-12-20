package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.interactor.exception.HikingLayerFileSaveFailedException
import hu.mostoha.mobile.android.huki.interactor.exception.TooManyRequestsException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.FileNotFoundException

class DomainExceptionMapperTest {

    @Test
    fun `Given HTTP 429 Too Many Requests exception, when map, then TooManyRequestsException returns`() {
        val exception = HttpException(Response.error<Unit>(429, "".toResponseBody()))

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(TooManyRequestsException(exception))
    }

    @Test
    fun `Given FileNotFoundException, when map, then HikingLayerFileSaveFailedException returns`() {
        val exception = FileNotFoundException()

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(HikingLayerFileSaveFailedException(exception))
    }

    @Test
    fun `Given IllegalStateException, when map, then UnknownException returns`() {
        val exception = IllegalStateException("Unknown error")

        val mappedException = DomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(UnknownException(exception))
    }

}

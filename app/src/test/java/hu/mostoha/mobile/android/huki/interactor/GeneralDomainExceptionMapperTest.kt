package hu.mostoha.mobile.android.huki.interactor

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import org.junit.Test

class GeneralDomainExceptionMapperTest {

    @Test
    fun `Given IllegalStateException, when map, then domain exception returns with unknown message`() {
        val exception = IllegalStateException("Unknown error")

        val mappedException = GeneralDomainExceptionMapper.map(exception)

        assertThat(mappedException).isEqualTo(DomainException(R.string.error_message_unknown, exception))
    }

}

package hu.mostoha.mobile.android.huki.interactor.exception

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.ui.util.toMessage

data class TimeoutException(
    val throwable: Throwable
) : DomainException(R.string.error_message_gateway_timeout.toMessage(), throwable)

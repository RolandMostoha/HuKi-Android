package hu.mostoha.mobile.android.huki.interactor.exception

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.toMessage

data class UnknownHostDomainException(
    val throwable: Throwable
) : DomainException(R.string.error_message_unknown_host.toMessage(), throwable)

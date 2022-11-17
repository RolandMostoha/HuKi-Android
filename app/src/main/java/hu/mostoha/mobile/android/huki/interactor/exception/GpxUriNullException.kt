package hu.mostoha.mobile.android.huki.interactor.exception

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.toMessage

data class GpxUriNullException(
    val throwable: Throwable
) : DomainException(R.string.error_message_gpx_uri_null.toMessage(), throwable)

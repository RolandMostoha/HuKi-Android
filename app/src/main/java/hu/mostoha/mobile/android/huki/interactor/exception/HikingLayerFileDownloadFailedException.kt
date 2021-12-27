package hu.mostoha.mobile.android.huki.interactor.exception

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.ui.util.toMessage

data class HikingLayerFileDownloadFailedException(
    val throwable: Throwable
) : DomainException(R.string.error_message_hiking_layer_file_download_failed.toMessage(), throwable)

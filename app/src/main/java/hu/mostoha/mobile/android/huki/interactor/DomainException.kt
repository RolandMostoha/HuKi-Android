package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.ui.util.Message

data class DomainException(
    val throwable: Throwable? = null,
    val messageRes: Message.Res
) : Exception(throwable)

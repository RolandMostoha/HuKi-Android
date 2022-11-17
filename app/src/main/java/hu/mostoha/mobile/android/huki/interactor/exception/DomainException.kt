package hu.mostoha.mobile.android.huki.interactor.exception

import hu.mostoha.mobile.android.huki.model.ui.Message

open class DomainException(
    val messageRes: Message.Res,
    throwable: Throwable? = null
) : Exception(throwable)

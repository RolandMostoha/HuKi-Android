package hu.mostoha.mobile.android.huki.interactor

import androidx.annotation.StringRes

open class DomainException(
    @StringRes val messageRes: Int,
    throwable: Throwable? = null
) : Exception(throwable)

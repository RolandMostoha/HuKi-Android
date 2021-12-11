package hu.mostoha.mobile.android.huki.interactor

import androidx.annotation.StringRes

data class DomainException(
    @StringRes val messageRes: Int,
    val throwable: Throwable? = null
) : Exception(throwable)

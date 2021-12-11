package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.R

object GeneralDomainExceptionMapper {

    fun map(exception: Exception) = DomainException(R.string.error_message_unknown, exception)

}

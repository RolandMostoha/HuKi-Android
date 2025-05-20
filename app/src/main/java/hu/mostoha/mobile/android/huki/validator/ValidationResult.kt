package hu.mostoha.mobile.android.huki.validator

import androidx.annotation.StringRes

sealed class ValidationResult {

    data object Success : ValidationResult()

    data class Error(@StringRes val messageRes: Int) : ValidationResult()

}

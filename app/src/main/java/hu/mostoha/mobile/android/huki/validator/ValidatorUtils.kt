package hu.mostoha.mobile.android.huki.validator

import hu.mostoha.mobile.android.huki.model.ui.InputField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun MutableStateFlow<InputField>.updateWithValidation(
    validators: List<Validator>,
    newInput: String? = null,
): Boolean {
    var isValid = false

    this.update { inputField ->
        val input = newInput ?: inputField.inputValue

        val firstError = validators
            .map { it.validate(input) }
            .filterIsInstance<ValidationResult.Error>()
            .firstOrNull()

        if (firstError == null) {
            isValid = true
        }

        if (newInput != null) {
            inputField.copy(
                inputValue = newInput,
                errorResId = firstError?.messageRes,
            )
        } else {
            inputField.copy(errorResId = firstError?.messageRes)
        }
    }

    return isValid
}

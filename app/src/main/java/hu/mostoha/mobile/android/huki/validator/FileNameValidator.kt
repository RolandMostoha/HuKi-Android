package hu.mostoha.mobile.android.huki.validator

import hu.mostoha.mobile.android.huki.R

class FileNameValidator(private val existingFilesNames: List<String>) : Validator {

    companion object {
        private val INVALID_CHARS = listOf("/", "\\", "\"", "*", "<", ">", "|", ":", "?", "=")
    }

    override fun validate(value: String): ValidationResult {
        return when {
            value.isEmpty() -> {
                ValidationResult.Error(R.string.gpx_history_rename_error_empty)
            }
            existingFilesNames.contains(value) -> {
                ValidationResult.Error(R.string.gpx_history_rename_error_already_exists)
            }
            containsInvalidCharacters(value) -> {
                ValidationResult.Error(R.string.gpx_history_rename_error_invalid_chars)
            }
            else -> ValidationResult.Success
        }
    }

    private fun containsInvalidCharacters(filename: String): Boolean {
        for (character in INVALID_CHARS) {
            if (filename.contains(character)) {
                return true
            }
        }
        return false
    }

}

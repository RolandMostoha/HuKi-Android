package hu.mostoha.mobile.android.huki.validator

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import org.junit.Test

class FileNameValidatorTest {

    private val validator = FileNameValidator(listOf("file1", "file2"))

    @Test
    fun `Given valid string, when validate, then validation success returns`() {
        val value = "file3"

        val result = validator.validate(value)

        assertThat(result).isEqualTo(ValidationResult.Success)
    }

    @Test
    fun `Given empty string, when validate, then validation error returns`() {
        val value = ""

        val result = validator.validate(value)

        assertThat(result).isEqualTo(ValidationResult.Error(R.string.gpx_history_rename_error_empty))
    }

    @Test
    fun `Given name from existing file names, when validate, then validation error returns`() {
        val value = "file1"

        val result = validator.validate(value)

        assertThat(result).isEqualTo(ValidationResult.Error(R.string.gpx_history_rename_error_already_exists))
    }

    @Test
    fun `Given name with invalid characters, when validate, then validation error returns`() {
        val value = "\\+@"

        val result = validator.validate(value)

        assertThat(result).isEqualTo(ValidationResult.Error(R.string.gpx_history_rename_error_invalid_chars))
    }

}

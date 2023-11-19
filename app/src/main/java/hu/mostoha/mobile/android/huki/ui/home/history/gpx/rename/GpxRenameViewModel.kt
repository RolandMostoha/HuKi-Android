package hu.mostoha.mobile.android.huki.ui.home.history.gpx.rename

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult
import hu.mostoha.mobile.android.huki.model.ui.InputField
import hu.mostoha.mobile.android.huki.validator.FileNameValidator
import hu.mostoha.mobile.android.huki.validator.updateWithValidation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GpxRenameViewModel @Inject constructor() : ViewModel() {

    lateinit var existingFilesNames: List<String>
    lateinit var gpxFileUri: Uri

    private val _fileNameInputField = MutableStateFlow(InputField())
    val fileNameInputField: StateFlow<InputField>
        get() = _fileNameInputField.asStateFlow()

    private val _gpxRenameEvents = MutableSharedFlow<GpxRenameEvents>()
    val gpxRenameEvents: SharedFlow<GpxRenameEvents> = _gpxRenameEvents.asSharedFlow()

    private val validators by lazy { listOf(FileNameValidator(existingFilesNames)) }

    fun onFileNameChanged(newFileName: String) {
        _fileNameInputField.updateWithValidation(validators, newFileName)
    }

    fun saveFile(newFileName: String) {
        viewModelScope.launch {
            val isValid = _fileNameInputField.updateWithValidation(validators, newFileName)

            if (isValid) {
                val gpxRenameResult = GpxRenameResult(gpxFileUri, newFileName)

                _gpxRenameEvents.emit(GpxRenameEvents.ValidationSuccess(gpxRenameResult))
            }
        }
    }

}

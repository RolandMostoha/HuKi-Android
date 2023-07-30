package hu.mostoha.mobile.android.huki.ui.home.gpx.history

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult
import hu.mostoha.mobile.android.huki.model.ui.InputField
import hu.mostoha.mobile.android.huki.ui.home.gpx.rename.GpxRenameEvents
import hu.mostoha.mobile.android.huki.ui.home.gpx.rename.GpxRenameViewModel
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GpxRenameViewModelTest {

    private lateinit var viewModel: GpxRenameViewModel

    private val gpxFileUri = mockk<Uri>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        viewModel = GpxRenameViewModel()
        viewModel.existingFilesNames = listOf("file1", "file2")
        viewModel.gpxFileUri = gpxFileUri
    }

    @Test
    fun `When init, then file name input field is empty string`() {
        runTestDefault {
            viewModel.fileNameInputField.test {
                assertThat(awaitItem()).isEqualTo(InputField())
            }
        }
    }

    @Test
    fun `Given valid new file name, when onFileNameChanged, then file name input field updates`() {
        runTestDefault {
            viewModel.fileNameInputField.test {
                viewModel.onFileNameChanged("new_file_name")

                assertThat(awaitItem()).isEqualTo(InputField())
                assertThat(awaitItem()).isEqualTo(InputField("new_file_name"))
            }
        }
    }

    @Test
    fun `Given empty new file name, when onFileNameChanged, then error file name input field updates`() {
        runTestDefault {
            viewModel.fileNameInputField.test {
                viewModel.onFileNameChanged("")

                assertThat(awaitItem()).isEqualTo(InputField())
                assertThat(awaitItem()).isEqualTo(InputField("", R.string.gpx_history_rename_error_empty))
            }
        }
    }

    @Test
    fun `Given valid new file name, when saveFile, then GPX rename result emitted`() {
        runTestDefault {
            viewModel.gpxRenameEvents.test {
                viewModel.saveFile("new_file_name")

                assertThat(awaitItem()).isEqualTo(
                    GpxRenameEvents.ValidationSuccess(GpxRenameResult(gpxFileUri, "new_file_name"))
                )
            }
        }
    }

}

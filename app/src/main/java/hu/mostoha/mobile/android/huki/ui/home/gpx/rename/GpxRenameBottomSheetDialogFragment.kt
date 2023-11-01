package hu.mostoha.mobile.android.huki.ui.home.gpx.rename

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.databinding.FragmentGpxRenameBottomSheetDialogBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GpxRenameBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private val TAG = GpxRenameBottomSheetDialogFragment::class.java.simpleName + ".TAG"

        private val ARG_FILE_URI = this::class.java.simpleName + "ARG_FILE_URI"
        private val ARG_EXISTING_FILE_NAMES = this::class.java.simpleName + "ARG_EXISTING_FILE_NAMES"

        fun showDialog(activity: FragmentActivity, fileUri: Uri, existingFileNames: List<String>) {
            newInstance(fileUri, existingFileNames).show(activity.supportFragmentManager, TAG)
        }

        private fun newInstance(fileUri: Uri, existingFileNames: List<String>): GpxRenameBottomSheetDialogFragment {
            return GpxRenameBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FILE_URI, fileUri)
                    putStringArrayList(ARG_EXISTING_FILE_NAMES, ArrayList(existingFileNames))
                }
            }
        }
    }

    private val gpxRenameViewModel: GpxRenameViewModel by viewModels()
    private val gpxRenameResultViewModel: GpxRenameSharedViewModel by activityViewModels()

    private var _binding: FragmentGpxRenameBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val gpxFileUri by lazy { requireArguments().getParcelable<Uri>(ARG_FILE_URI)!! }
    private val existingGpxFileNames by lazy { requireArguments().getStringArrayList(ARG_EXISTING_FILE_NAMES)!! }

    private val gpxRenameInputLayout by lazy { binding.gpxRenameInputLayout }
    private val gpxRenameInput by lazy { binding.gpxRenameInput }
    private val gpxRenameCancelButton by lazy { binding.gpxRenameCancelButton }
    private val gpxRenameSaveButton by lazy { binding.gpxRenameSaveButton }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGpxRenameBottomSheetDialogBinding.inflate(inflater, container, false)

        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as View).clearBackground()

        initDialog()
        initViews()
        initFlows()

        gpxRenameViewModel.existingFilesNames = existingGpxFileNames
        gpxRenameViewModel.gpxFileUri = gpxFileUri
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initDialog() {
        val sheet = requireDialog() as BottomSheetDialog
        sheet.behavior.skipCollapsed = true
        sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initViews() {
        gpxRenameInput.addTextChangedListener { editable ->
            gpxRenameViewModel.onFileNameChanged(editable.toString())
        }
        gpxRenameSaveButton.setOnClickListener {
            lifecycleScope.launch {
                gpxRenameViewModel.saveFile(gpxRenameInput.text.toString())
            }
        }
        gpxRenameCancelButton.setOnClickListener {
            gpxRenameResultViewModel.clearResult()

            dismiss()
        }
    }

    private fun initFlows() {
        lifecycleScope.launch {
            gpxRenameViewModel.fileNameInputField
                .flowWithLifecycle(lifecycle)
                .collect {
                    if (it.errorResId == null) {
                        gpxRenameInputLayout.error = null
                    } else {
                        gpxRenameInputLayout.error = getString(it.errorResId)
                    }
                }
        }
        lifecycleScope.launch {
            gpxRenameViewModel.gpxRenameEvents
                .flowWithLifecycle(lifecycle)
                .collect { gpxRenameEvents ->
                    if (gpxRenameEvents is GpxRenameEvents.ValidationSuccess) {
                        gpxRenameResultViewModel.updateResult(gpxRenameEvents.gpxRenameResult)

                        dismiss()
                    }
                }
        }
    }

}

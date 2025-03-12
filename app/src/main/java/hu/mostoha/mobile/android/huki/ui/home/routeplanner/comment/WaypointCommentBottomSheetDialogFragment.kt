package hu.mostoha.mobile.android.huki.ui.home.routeplanner.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.databinding.FragmentWaypointCommentBottomSheetBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import hu.mostoha.mobile.android.huki.model.ui.WaypointComment
import hu.mostoha.mobile.android.huki.model.ui.WaypointCommentResult
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WaypointCommentBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private val TAG = WaypointCommentBottomSheetDialogFragment::class.java.simpleName + ".TAG"

        private val ARG_COMMENT_RESULT = this::class.java.simpleName + "ARG_WAYPOINT_ITEM"

        fun showDialog(activity: FragmentActivity, commentResult: WaypointCommentResult) {
            newInstance(commentResult).show(activity.supportFragmentManager, TAG)
        }

        private fun newInstance(commentResult: WaypointCommentResult): WaypointCommentBottomSheetDialogFragment {
            return WaypointCommentBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_COMMENT_RESULT, commentResult)
                }
            }
        }
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val commentResultViewModel: WaypointCommentResultViewModel by activityViewModels()

    private var _binding: FragmentWaypointCommentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val commentResult by lazy { requireArguments().getParcelable<WaypointCommentResult>(ARG_COMMENT_RESULT)!! }

    private val commentNameInput by lazy { binding.waypointCommentNameInput }
    private val commentInput by lazy { binding.waypointCommentInput }
    private val cancelButton by lazy { binding.waypointCommentCancelButton }
    private val saveButton by lazy { binding.waypointCommentSaveButton }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWaypointCommentBottomSheetBinding.inflate(inflater, container, false)

        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as View).clearBackground()

        initDialog()
        initViews()
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
        commentNameInput.setText(commentResult.waypointComment.name)
        commentInput.setText(commentResult.waypointComment.comment)
        saveButton.setOnClickListener {
            analyticsService.routePlannerCommentDone()

            lifecycleScope.launch {
                commentResultViewModel.updateResult(
                    commentResult.copy(
                        waypointComment = WaypointComment(
                            name = commentNameInput.text.toString(),
                            comment = commentInput.text.toString()
                        ),
                    )
                )
            }

            dismiss()
        }
        cancelButton.setOnClickListener {
            commentResultViewModel.clearResult()

            dismiss()
        }
    }

}

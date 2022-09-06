package hu.mostoha.mobile.android.huki.ui.home.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentContactBottomSheetDialogBinding
import hu.mostoha.mobile.android.huki.extensions.hyperlinkStyle
import hu.mostoha.mobile.android.huki.extensions.startEmailIntent
import hu.mostoha.mobile.android.huki.extensions.startUrlIntent

class ContactBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG = ContactBottomSheetDialogFragment::class.java.simpleName + ".TAG"
    }

    private var _binding: FragmentContactBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactBottomSheetDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.contactEmail.hyperlinkStyle()
        binding.contactEmail.setOnClickListener {
            requireContext().startEmailIntent(
                email = getString(R.string.contact_email),
                subject = getString(R.string.contact_email_subject)
            )
        }

        binding.contactGitHub.hyperlinkStyle()
        binding.contactGitHub.setOnClickListener {
            requireContext().startUrlIntent(getString(R.string.contact_github_repository_url))
        }
    }

}

package hu.mostoha.mobile.android.huki.ui.home.newfeatures

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentNewFeaturesBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.extensions.toMonthFormat
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.NewFeatures
import hu.mostoha.mobile.android.huki.ui.home.settings.SettingsViewModel
import hu.mostoha.mobile.android.huki.ui.home.support.ProductsViewModel
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.util.productBackgroundColor
import hu.mostoha.mobile.android.huki.util.productIconColor
import hu.mostoha.mobile.android.huki.util.productStrokeColor
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewFeaturesBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private val TAG = NewFeaturesBottomSheetDialogFragment::class.java.simpleName + ".TAG"
        private val ARG_NEW_FEATURES = this::class.java.simpleName + "ARG_NEW_FEATURES"

        fun showDialog(activity: FragmentActivity, newFeatures: NewFeatures) {
            newInstance(newFeatures).show(activity.supportFragmentManager, TAG)
        }

        private fun newInstance(newFeatures: NewFeatures): NewFeaturesBottomSheetDialogFragment {
            return NewFeaturesBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_NEW_FEATURES, newFeatures)
                }
            }
        }
    }

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val productsViewModel: ProductsViewModel by activityViewModels()

    private var _binding: FragmentNewFeaturesBinding? = null
    private val binding get() = _binding!!

    private val newFeatures by lazy { requireArguments().getParcelable<NewFeatures>(ARG_NEW_FEATURES)!! }

    private val newFeaturesVersion by lazy { binding.newFeaturesVersion }
    private val newFeaturesReleaseDate by lazy { binding.newFeaturesReleaseDate }
    private val newFeaturesMessage by lazy { binding.newFeaturesMessage }
    private val closeButton by lazy { binding.newFeaturesCloseButton }
    private val facebookJoinButton by lazy { binding.newFeaturesFacebookJoinButton }
    private val supporterCard by lazy { binding.newFeaturesSupporterCard }
    private val supporterBadgeImage by lazy { binding.newFeaturesSupporterBadgeImage }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewFeaturesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as View).clearBackground()

        initDialog()
        initViews()
        initFlows()

        settingsViewModel.updateNewFeaturesSeen(BuildConfig.VERSION_NAME)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initDialog() {
        val sheet = requireDialog() as BottomSheetDialog
        sheet.behavior.skipCollapsed = true
        sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheet.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    private fun initViews() {
        newFeaturesVersion.text = newFeatures.version
        newFeaturesReleaseDate.text = newFeatures.releaseDate.toMonthFormat()
        newFeaturesMessage.text = newFeatures.releaseNotes
        closeButton.setOnClickListener {
            dismiss()
        }
        facebookJoinButton.setOnClickListener {
            requireContext().openUrl(getString(R.string.facebook_page_url))
        }
    }

    private fun initFlows() {
        lifecycleScope.launch {
            productsViewModel.productsUiModel
                .map { it.purchases }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { purchases ->
                    if (purchases.isNotEmpty()) {
                        val lastPurchasedProduct = purchases.map { it.productType }.first()
                        val productColor = requireContext().color(lastPurchasedProduct.productColorRes)
                        supporterBadgeImage.imageTintList = productColor
                            .productIconColor(requireContext())
                            .colorStateList()
                        supporterBadgeImage.setImageResource(lastPurchasedProduct.productIcon)
                        supporterCard.setCardBackgroundColor(productColor.productBackgroundColor(requireContext()))
                        supporterCard.strokeColor = productColor.productStrokeColor(requireContext())
                        supporterCard.visible()
                    } else {
                        supporterCard.gone()
                    }
                }
        }
    }

}

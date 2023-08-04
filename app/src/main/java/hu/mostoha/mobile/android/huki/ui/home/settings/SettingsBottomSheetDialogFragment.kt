package hu.mostoha.mobile.android.huki.ui.home.settings

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentSettingsBottomSheetDialogBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import hu.mostoha.mobile.android.huki.extensions.hyperlinkStyle
import hu.mostoha.mobile.android.huki.extensions.startEmailIntent
import hu.mostoha.mobile.android.huki.extensions.startUrlIntent
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.toPercentageFromScale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG = SettingsBottomSheetDialogFragment::class.java.simpleName + ".TAG"

        private const val MAP_SCALE_FACTOR_FROM = 100
        private const val MAP_SCALE_FACTOR_TO = 300
        private const val MAP_SCALE_FACTOR_STEP = 10
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private var _binding: FragmentSettingsBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val mapScaleInfoButton by lazy { binding.settingsMapScaleInfoButton }
    private val mapScaleSliderFromText by lazy { binding.settingsMapScaleSliderFromText }
    private val mapScaleSliderToText by lazy { binding.settingsMapScaleSliderToText }
    private val mapScaleSlider by lazy { binding.settingsMapScaleSlider }
    private val themeSystemRadioButton by lazy { binding.settingsThemeSystem }
    private val themeLightRadioButton by lazy { binding.settingsThemeLight }
    private val themeDarkRadioButton by lazy { binding.settingsThemeDark }
    private val offlineModeInfoButton by lazy { binding.settingsOfflineModeInfoButton }
    private val emailText by lazy { binding.settingsEmailText }
    private val gitHubText by lazy { binding.settingsGitHubText }
    private val googlePlayReviewButton by lazy { binding.settingsGooglePlayReviewButton }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBottomSheetDialogBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view.parent as View).clearBackground()

        initDialog()
        initViews()
        initFlows()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        analyticsService.settingsMapScaleSet(mapScaleSlider.value.toLong())
    }

    private fun initDialog() {
        val sheet = requireDialog() as BottomSheetDialog
        sheet.behavior.skipCollapsed = true
        sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initViews() {
        mapScaleSliderFromText.text = getString(R.string.common_percentage, MAP_SCALE_FACTOR_FROM)
        mapScaleSliderToText.text = getString(R.string.common_percentage, MAP_SCALE_FACTOR_TO)
        mapScaleSlider.apply {
            valueFrom = MAP_SCALE_FACTOR_FROM.toFloat()
            valueTo = MAP_SCALE_FACTOR_TO.toFloat()
            stepSize = MAP_SCALE_FACTOR_STEP.toFloat()
            setLabelFormatter { getString(R.string.common_percentage, it.toInt()) }
            addOnChangeListener { _, value, _ ->
                settingsViewModel.updateMapScale(value.toInt())
            }
        }
        mapScaleInfoButton.onClick = {
            analyticsService.settingsMapScaleInfoClicked()
        }
        themeSystemRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settingsViewModel.updateTheme(Theme.SYSTEM)
            }
        }
        themeLightRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settingsViewModel.updateTheme(Theme.LIGHT)
            }
        }
        themeDarkRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settingsViewModel.updateTheme(Theme.DARK)
            }
        }
        offlineModeInfoButton.onClick = {
            analyticsService.settingsOfflineModeInfoClicked()
        }
        emailText.hyperlinkStyle()
        emailText.setOnClickListener {
            analyticsService.settingsEmailClicked()
            requireContext().startEmailIntent(
                email = getString(R.string.settings_email),
                subject = getString(R.string.settings_email_subject)
            )
        }
        gitHubText.hyperlinkStyle()
        gitHubText.setOnClickListener {
            analyticsService.settingsGitHubClicked()
            requireContext().startUrlIntent(getString(R.string.settings_github_repository_url))
        }
        googlePlayReviewButton.setOnClickListener {
            analyticsService.settingsGooglePlayReviewClicked()
            requireContext().startUrlIntent(getString(R.string.settings_google_play_review_url))
        }
    }

    private fun initFlows() {
        lifecycleScope.launch {
            val mapScaleFactor = settingsViewModel.mapScaleFactor
                .flowWithLifecycle(lifecycle)
                .first()

            mapScaleSlider.value = mapScaleFactor.toPercentageFromScale().toFloat()
        }
        lifecycleScope.launch {
            val theme = settingsViewModel.theme
                .flowWithLifecycle(lifecycle)
                .first()

            when (theme) {
                Theme.SYSTEM -> themeSystemRadioButton.isChecked = true
                Theme.LIGHT -> themeLightRadioButton.isChecked = true
                Theme.DARK -> themeDarkRadioButton.isChecked = true
            }
        }
    }

}

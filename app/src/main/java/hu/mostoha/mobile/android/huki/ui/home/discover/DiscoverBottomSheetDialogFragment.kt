package hu.mostoha.mobile.android.huki.ui.home.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentDiscoverBottomSheetDialogBinding
import hu.mostoha.mobile.android.huki.extensions.clearBackground
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.adapter.PlaceCategoryAdapter
import hu.mostoha.mobile.android.huki.ui.adapter.PlaceCategoryAdapter.Companion.addVerticalChip
import hu.mostoha.mobile.android.huki.ui.home.HomeViewModel
import hu.mostoha.mobile.android.huki.util.KEKTURA_URL
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG = DiscoverBottomSheetDialogFragment::class.java.simpleName + ".TAG"
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var _binding: FragmentDiscoverBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val hikeRecommendationInfo by lazy { binding.discoverHikeRecommendationsInfo }
    private val oktRoutesInfo by lazy { binding.discoverOktRoutesInfo }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBottomSheetDialogBinding.inflate(inflater, container, false)

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
        with(binding.discoverHeaderContainer) {
            headerImage.setImageResource(R.drawable.ic_hungary_solid)
            headerTitle.text = requireContext().getString(R.string.discover_title)
            headerSubTitle.gone()
            headerCloseButton.setOnClickListener {
                dismiss()
            }
        }

        initLandscapes()
        initHikeRecommendations()
        initOktRoutes()
    }

    private fun initLandscapes() {
        binding.discoverLandscapesChipGroup.removeAllViews()
        binding.discoverLandscapesChipGroup.addVerticalChip(
            title = getString(R.string.discover_landscapes_button_title).toMessage(),
            iconRes = R.drawable.ic_hungary_colored,
            horizontalPadding = R.dimen.space_huge,
            isStroked = false,
            onClick = {
                analyticsService.landscapeMapClicked()
                homeViewModel.loadLandscapeMap()
                dismiss()
            }
        )
    }

    private fun initHikeRecommendations() {
        hikeRecommendationInfo.onOpen = {
            analyticsService.hikeRecommenderInfoClicked()
        }
        val placeCategoryAdapter = PlaceCategoryAdapter(requireContext())
        placeCategoryAdapter.initHikeRecommendations(
            chipGroup = binding.discoverHikeRecommendationsChipGroup,
            isStroked = false,
            onRecommendationClick = { hikeRecommendation ->
                analyticsService.hikeRecommendationClicked(hikeRecommendation)
                requireContext().openUrl(hikeRecommendation.baseUrl)
            },
        )
    }

    private fun initOktRoutes() {
        oktRoutesInfo.onContentClick = {
            requireContext().openUrl(KEKTURA_URL)
        }
        binding.discoverOktRoutesChipGroup.removeAllViews()
        OktType.entries.forEach { oktType ->
            binding.discoverOktRoutesChipGroup.addVerticalChip(
                title = oktType.title.toMessage(),
                subTitle = oktType.subTitle.toMessage(),
                iconRes = oktType.icon,
                isStroked = false,
                onClick = {
                    analyticsService.oktClicked(oktType)
                    homeViewModel.loadOktRoutes(oktType)
                    dismiss()
                }
            )
        }
    }

}

package hu.mostoha.mobile.android.huki.ui.home.placecategory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.FragmentPlaceCategoryBinding
import hu.mostoha.mobile.android.huki.extensions.addFragment
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.center
import hu.mostoha.mobile.android.huki.model.mapper.HikeRecommenderMapper
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.adapter.PlaceCategoryAdapter
import hu.mostoha.mobile.android.huki.ui.adapter.PlaceCategoryAdapter.Companion.addChip
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaceCategoryFragment : Fragment() {

    companion object {
        private val ARG_BOUNDING_BOX = this::class.java.simpleName + "ARG_BOUNDING_BOX"

        fun addFragment(fragmentManager: FragmentManager, @IdRes containerId: Int, boundingBox: BoundingBox) {
            fragmentManager.addFragment(
                containerId,
                PlaceCategoryFragment::class.java,
                Bundle().apply {
                    putParcelable(ARG_BOUNDING_BOX, boundingBox)
                }
            )
        }
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    private val insetSharedViewModel: InsetSharedViewModel by activityViewModels()
    private val placeCategoryEventViewModel: PlaceCategoryEventViewModel by activityViewModels()
    private val placeCategoryViewModel: PlaceCategoryViewModel by viewModels()

    private var _binding: FragmentPlaceCategoryBinding? = null
    private val binding get() = _binding!!

    private val boundingBox by lazy { requireArguments().getParcelable<BoundingBox>(ARG_BOUNDING_BOX) }

    private val container by lazy { binding.placeCategoryContainer }
    private val placeHeaderContainer by lazy { binding.placeCategoryHeaderContainer }
    private val placeCategoryGroups by lazy { binding.placeCategoryGroups }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceCategoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initFlows()

        boundingBox?.let { boundingBox ->
            placeCategoryViewModel.loadPlaceArea(boundingBox.center(), boundingBox)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initViews() {
        placeHeaderContainer.placeHeaderCloseButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        placeHeaderContainer.placeHeaderImage.setImageResource(R.drawable.ic_place_category_city)

        val placeCategoryAdapter = PlaceCategoryAdapter(requireContext())

        placeCategoryAdapter.initHikeRecommendations(
            chipGroup = binding.placeCategoryHikeRecommendationsChipGroup,
            isStroked = false,
            onHikingRoutesClick = {
                analyticsService.loadHikingRoutesClicked()

                val placeArea = placeCategoryViewModel.placeCategoryUiModel.value.placeArea
                val boundingBox = boundingBox

                if (placeArea != null && boundingBox != null) {
                    placeCategoryEventViewModel.updateEvent(PlaceCategoryEvent.HikingRouteSelected(placeArea))
                    requireActivity().supportFragmentManager.popBackStack()
                }
            },
            onKirandulastippekClick = {
                analyticsService.hikeRecommenderKirandulastippekClicked()

                val placeArea = placeCategoryViewModel.placeCategoryUiModel.value.placeArea

                if (placeArea != null) {
                    val url = HikeRecommenderMapper.getKirandulastippekLink(placeArea)
                    requireContext().openUrl(url)
                }
            },
            onTermeszetjaroClick = {
                analyticsService.hikeRecommenderTermeszetjaroClicked()

                val placeArea = placeCategoryViewModel.placeCategoryUiModel.value.placeArea

                if (placeArea != null) {
                    val url = HikeRecommenderMapper.getTermeszetjaroLink(placeArea)
                    requireContext().openUrl(url)
                }
            }
        )

        placeCategoryAdapter.initPlaceCategories(
            containerView = placeCategoryGroups,
            isStroked = false,
            onCategoryClick = {
                analyticsService.placeCategoryClicked(it)
                placeCategoryEventViewModel.updateEvent(PlaceCategoryEvent.PlaceCategorySelected(it))
                requireActivity().supportFragmentManager.popBackStack()
            }
        )
    }

    private fun initFlows() {
        lifecycleScope.launch {
            insetSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (result != null) {
                        container.updatePadding(
                            top = resources.getDimensionPixelSize(R.dimen.space_small) + result.topInset,
                        )
                    }
                }
        }
        lifecycleScope.launch {
            placeCategoryViewModel.placeCategoryUiModel
                .map { it.landscapes }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { landscapes ->
                    landscapes?.let { initLandscapes(it) }
                }
        }
        lifecycleScope.launch {
            placeCategoryViewModel.placeCategoryUiModel
                .map { it.placeArea }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { uiModel ->
                    placeHeaderContainer.placeHeaderTitle.text = uiModel?.addressMessage?.resolve(requireContext())
                    placeHeaderContainer.placeHeaderSubTitle.text = uiModel?.distanceMessage?.resolve(requireContext())
                }
        }
        lifecycleScope.launch {
            placeCategoryViewModel.placeCategoryUiModel
                .map { it.isAreaLoading }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { isLoading ->
                    if (isLoading) {
                        placeHeaderContainer.placeHeaderContentContainer.gone()
                        placeHeaderContainer.placeHeaderShimmer.visible()
                        placeHeaderContainer.placeHeaderShimmer.startShimmer()
                    } else {
                        placeHeaderContainer.placeHeaderContentContainer.visible()
                        placeHeaderContainer.placeHeaderShimmer.gone()
                        placeHeaderContainer.placeHeaderShimmer.hideShimmer()
                    }
                }
        }

    }

    private fun initLandscapes(landscapes: List<LandscapeUiModel>) {
        binding.placeCategoryLandscapeChipGroup.removeAllViews()
        landscapes.forEach { landscape ->
            binding.placeCategoryLandscapeChipGroup.addChip(
                title = landscape.name,
                isStroked = false,
                onClick = {
                    analyticsService.loadLandscapeClicked(landscape.name.resolve(requireContext()))
                    placeCategoryEventViewModel.updateEvent(PlaceCategoryEvent.LandscapeSelected(landscape))
                    requireActivity().supportFragmentManager.popBackStack()
                }
            )
        }
    }

}

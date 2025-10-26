package hu.mostoha.mobile.android.huki.ui.home.placecategory

import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetPlaceCategoryBinding
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.domain.Destination
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.mapper.HikeRecommendationMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.adapter.PlaceCategoryAdapter
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class PlaceCategoryBottomSheetDialog(
    private val binding: LayoutBottomSheetPlaceCategoryBinding,
    private val analyticsService: AnalyticsService
) : BottomSheetDialog(binding) {

    fun init(
        placeArea: PlaceArea,
        onHikingTrailsClick: () -> Unit,
        onCategoryClick: (PlaceCategory) -> Unit,
        onDestinationClick: (Destination) -> Unit,
        onCloseClick: () -> Unit,
    ) {
        with(binding.placeCategoryBottomSheetHeaderContainer) {
            placeHeaderTitle.text = placeArea.addressMessage.resolve(context)
            placeHeaderSubTitle.text = placeArea.distanceMessage.resolve(context)
            placeHeaderImage.setImageResource(placeArea.iconRes)
            placeHeaderCloseButton.setOnClickListener {
                onCloseClick.invoke()
            }
        }

        binding.placeCategoryBottomSheetDestinationsInfo.onOpen = {
            analyticsService.destinationInfoClicked()
        }
        binding.placeCategoryBottomSheetHikeRecommendationsInfo.setOnClickListener {
            analyticsService.hikeRecommenderInfoClicked()
            onHikingTrailsClick.invoke()
        }

        val adapter = PlaceCategoryAdapter(context)
        val placeAreaType = placeArea.placeAreaType

        val isLandscape = placeAreaType is PlaceAreaType.Landscape
        if (isLandscape) {
            adapter.initDestinations(
                binding.placeCategoryBottomSheetDestinationsChipGroup,
                placeAreaType.destinations,
                onDestinationClick = { destination ->
                    analyticsService.destinationClicked(destination.name)
                    onDestinationClick.invoke(destination)
                }
            )
        }
        binding.placeCategoryBottomSheetDestinationsContainer.visibleOrGone(isLandscape)

        adapter.initHikeRecommendations(
            chipGroup = binding.placeCategoryBottomSheetHikeRecommendationsChipGroup,
            isStroked = true,
            onRecommendationClick = { hikeRecommendation ->
                analyticsService.hikeRecommendationClicked(hikeRecommendation)
                context.openUrl(HikeRecommendationMapper.getNavigationLink(hikeRecommendation, placeArea))
            },
        )
        adapter.initPlaceCategories(
            containerView = binding.placeCategoryBottomSheetGroups,
            isStroked = true,
            onCategoryClick = { category ->
                if (category == PlaceCategory.HIKING_ROUTES) {
                    analyticsService.loadHikingRoutesClicked()
                    onHikingTrailsClick.invoke()
                } else {
                    analyticsService.placeCategoryClicked(category)
                    onCategoryClick.invoke(category)
                }
                hide()
            }
        )
    }

}

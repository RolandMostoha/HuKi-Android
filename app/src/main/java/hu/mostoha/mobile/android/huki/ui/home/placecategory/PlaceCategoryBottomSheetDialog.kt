package hu.mostoha.mobile.android.huki.ui.home.placecategory

import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetPlaceCategoryBinding
import hu.mostoha.mobile.android.huki.extensions.startUrlIntent
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.mapper.HikeRecommenderMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
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
        onCloseClick: () -> Unit,
    ) {
        val adapter = PlaceCategoryAdapter(context)

        with(binding.placeCategoryBottomSheetHeaderContainer) {
            placeHeaderTitle.text = placeArea.addressMessage.resolve(context)
            placeHeaderSubTitle.text = placeArea.distanceMessage.resolve(context)
            placeHeaderImage.setImageResource(placeArea.iconRes)
            placeHeaderCloseButton.setOnClickListener {
                onCloseClick.invoke()
            }
        }

        adapter.initHikeRecommendations(
            chipGroup = binding.placeCategoryBottomSheetHikeRecommendationsChipGroup,
            isStroked = true,
            onHikingRoutesClick = {
                analyticsService.loadHikingRoutesClicked()
                onHikingTrailsClick.invoke()
                hide()
            },
            onKirandulastippekClick = {
                analyticsService.hikeRecommenderKirandulastippekClicked()
                context.startUrlIntent(HikeRecommenderMapper.getKirandulastippekLink(placeArea))
            },
            onTermeszetjaroClick = {
                analyticsService.hikeRecommenderTermeszetjaroClicked()
                context.startUrlIntent(HikeRecommenderMapper.getTermeszetjaroLink(placeArea))
            }
        )
        adapter.initPlaceCategories(
            containerView = binding.placeCategoryBottomSheetGroups,
            isStroked = true,
            onCategoryClick = { category ->
                analyticsService.placeCategoryClicked(category)
                onCategoryClick.invoke(category)
                hide()
            }
        )
    }

}

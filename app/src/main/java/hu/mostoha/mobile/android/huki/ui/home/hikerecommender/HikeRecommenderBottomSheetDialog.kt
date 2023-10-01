package hu.mostoha.mobile.android.huki.ui.home.hikerecommender

import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetHikeRecommenderBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.startUrlIntent
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class HikeRecommenderBottomSheetDialog(
    private val binding: LayoutBottomSheetHikeRecommenderBinding,
    private val analyticsService: AnalyticsService
) : BottomSheetDialog(binding) {

    fun init(
        hikeRecommendation: HikeRecommendation,
        isInfoButtonEnabled: Boolean,
        onHikingTrailsClick: () -> Unit,
        onCloseClick: () -> Unit,
        onCloseInfoTextClick: () -> Unit,
    ) {
        postMain {
            with(binding) {
                val placeName = hikeRecommendation.title.resolve(root.context)
                val iconRes = hikeRecommendation.iconRes
                val kirandulastippekLink = hikeRecommendation.kirandulastippekLink
                val termeszetjaroLink = hikeRecommendation.termeszetjaroLink

                hikeRecommenderInfoText.visibleOrGone(isInfoButtonEnabled)

                val context = binding.root.context

                hikeRecommenderNameText.text = placeName
                hikeRecommenderImage.setImageResource(iconRes)
                hikeRecommenderHikingTrailsButton.setOnClickListener {
                    analyticsService.loadHikingRoutesClicked(placeName)
                    onHikingTrailsClick.invoke()
                }
                hikeRecommenderKirandulastippekButton.setOnClickListener {
                    analyticsService.hikeRecommenderKirandulastippekClicked(placeName)
                    context.startUrlIntent(kirandulastippekLink)
                }
                hikeRecommenderTermeszetjaroButton.setOnClickListener {
                    analyticsService.hikeRecommenderTermeszetjaroClicked(placeName)
                    context.startUrlIntent(termeszetjaroLink)
                }
                hikeRecommenderCloseButton.setOnClickListener {
                    onCloseClick.invoke()
                }
                hikeRecommenderInfoText.setOnClickListener {
                    hikeRecommenderInfoText.gone()
                    onCloseInfoTextClick.invoke()
                }
            }
            show()
        }
    }

}

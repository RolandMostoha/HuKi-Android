package hu.mostoha.mobile.android.huki.ui.home.landscapedetails

import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetLandscapeDetailsBinding
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.startUrlIntent
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class LandscapeDetailsBottomSheetDialog(
    private val binding: LayoutBottomSheetLandscapeDetailsBinding,
    private val analyticsService: FirebaseAnalyticsService
) : BottomSheetDialog(binding) {

    fun initLandscapeDetailsBottomSheet(
        landscapeDetailsUiModel: LandscapeDetailsUiModel,
        onHikingTrailsButtonClick: () -> Unit,
        onCloseButtonClick: () -> Unit
    ) {
        postMain {
            with(binding) {
                val context = binding.root.context
                val placeName = landscapeDetailsUiModel.landscapeUiModel.name.resolve(root.context)

                landscapeDetailsNameText.text = placeName
                landscapeDetailsImage.setImageResource(landscapeDetailsUiModel.landscapeUiModel.iconRes)
                landscapeDetailsHikingTrailsButton.setOnClickListener {
                    analyticsService.loadHikingRoutesClicked(placeName)
                    onHikingTrailsButtonClick.invoke()
                }
                landscapeDetailsKirandulastippekButton.setOnClickListener {
                    analyticsService.landscapeKirandulastippekClicked(placeName)
                    context.startUrlIntent(landscapeDetailsUiModel.landscapeUiModel.kirandulastippekLink)
                }
                landscapeDetailsKirandulastippekInfoButton.onClick = {
                    analyticsService.landscapeKirandulastippekInfoClicked()
                }
                landscapeDetailsTermeszetjaroButton.setOnClickListener {
                    analyticsService.landscapeTermeszetjaroClicked(placeName)
                    context.startUrlIntent(landscapeDetailsUiModel.landscapeUiModel.termeszetjaroLink)
                }
                landscapeDetailsTermeszetjaroInfoButton.onClick = {
                    analyticsService.landscapeTermeszetjaroInfoClicked()
                }
                landscapeDetailsCloseButton.setOnClickListener {
                    onCloseButtonClick.invoke()
                }
            }
            show()
        }
    }

}

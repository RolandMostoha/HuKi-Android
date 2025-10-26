package hu.mostoha.mobile.android.huki.ui.home.landscapes

import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetLandscapesBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.postMainDelayed
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.RECYCLERVIEW_SCROLL_DELAY
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class LandscapesBottomSheetDialog(
    private val binding: LayoutBottomSheetLandscapesBinding,
    private val analyticsService: AnalyticsService
) : BottomSheetDialog(binding) {

    private var landscapesAdapter: LandscapesAdapter? = null

    override fun updateInset(insets: Insets) {
        binding.landscapesList.updatePadding(
            bottom = insets.bottom + resources.getDimensionPixelSize(R.dimen.space_large)
        )
    }

    fun init(
        landscapes: List<LandscapeDetailsUiModel>,
        selectedOsmId: String?,
        onLandscapeClick: (LandscapeUiModel) -> Unit,
        onAllLandscapesClick: () -> Unit,
        onDetailsClick: (String) -> Unit,
        onCloseClick: () -> Unit,
    ) {
        postMain {
            with(binding) {
                landscapesHeaderContainer.headerImage.setImageResource(R.drawable.ic_hungary_colored)
                landscapesHeaderContainer.headerImage.setOnClickListener {
                    onAllLandscapesClick.invoke()
                }
                landscapesHeaderContainer.headerTitle.text = context.getString(R.string.landscapes_title)
                landscapesHeaderContainer.headerSubTitle.gone()
                landscapesHeaderContainer.headerCloseButton.setOnClickListener { onCloseClick.invoke() }

                if (landscapesAdapter == null) {
                    landscapesAdapter = LandscapesAdapter(
                        onItemClick = { landscapeUiModel ->
                            analyticsService.landscapeClicked(landscapeUiModel.name.resolve(context))
                            onLandscapeClick.invoke(landscapeUiModel)
                        },
                        onDetailsClick = { landscapeUiModel ->
                            onDetailsClick.invoke(landscapeUiModel.osmId)
                        }
                    )
                    landscapesList.setHasFixedSize(true)
                    landscapesList.adapter = landscapesAdapter
                }
            }

            landscapesAdapter?.submitList(landscapes)

            show()

            selectedOsmId?.let {
                postMainDelayed(RECYCLERVIEW_SCROLL_DELAY) {
                    scrollTo(selectedOsmId)
                }
            }
        }
    }

    private fun scrollTo(osmId: String) {
        landscapesAdapter?.let { adapter ->
            val index = adapter.indexOf(osmId)

            binding.landscapesList.smoothScrollToPosition(index)
        }
    }

}

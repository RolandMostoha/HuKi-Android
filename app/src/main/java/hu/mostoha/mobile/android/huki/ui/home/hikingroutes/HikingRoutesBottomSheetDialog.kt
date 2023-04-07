package hu.mostoha.mobile.android.huki.ui.home.hikingroutes

import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetHikingRoutesBinding
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

class HikingRoutesBottomSheetDialog(
    private val binding: LayoutBottomSheetHikingRoutesBinding
) : BottomSheetDialog(binding) {

    fun initBottomSheet(
        hikingRoutes: List<HikingRoutesItem>,
        onHikingRouteClick: (HikingRouteUiModel) -> Unit,
        onCloseClick: () -> Unit
    ) {
        postMain {
            with(binding) {
                val hikingRoutesAdapter = HikingRoutesAdapter(
                    onItemClick = { hikingRoute ->
                        onHikingRouteClick.invoke(hikingRoute)
                        hide()
                    },
                    onCloseClick = {
                        onCloseClick.invoke()
                        hide()
                    }
                )
                hikingRoutesList.setHasFixedSize(true)
                hikingRoutesList.adapter = hikingRoutesAdapter
                hikingRoutesAdapter.submitList(hikingRoutes)
            }
            show()
        }
    }

}

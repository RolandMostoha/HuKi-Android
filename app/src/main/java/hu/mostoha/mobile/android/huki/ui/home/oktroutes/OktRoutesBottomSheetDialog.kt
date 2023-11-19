package hu.mostoha.mobile.android.huki.ui.home.oktroutes

import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetOktRoutesBinding
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.postMainDelayed
import hu.mostoha.mobile.android.huki.extensions.startUrlIntent
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.RECYCLERVIEW_SCROLL_DELAY
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog
import org.osmdroid.util.GeoPoint

class OktRoutesBottomSheetDialog(
    private val binding: LayoutBottomSheetOktRoutesBinding,
    private val analyticsService: AnalyticsService
) : BottomSheetDialog(binding) {

    private var oktRoutesAdapter: OktRoutesAdapter? = null

    fun init(
        oktRoutes: List<OktRouteUiModel>,
        selectedOktId: String,
        onRouteClick: (String) -> Unit,
        onEdgePointClick: (GeoPoint) -> Unit,
        onCloseClick: () -> Unit,
    ) {
        postMain {
            with(binding) {
                if (oktRoutesAdapter == null) {
                    oktRoutesAdapter = OktRoutesAdapter(
                        onItemClick = { oktId ->
                            analyticsService.oktRouteClicked(oktId)
                            onRouteClick.invoke(oktId)
                        },
                        onLinkClick = { oktId, link ->
                            analyticsService.oktRouteLinkClicked(oktId)
                            context.startUrlIntent(link)
                        },
                        onEdgePointClick = { oktId, geoPoint ->
                            analyticsService.oktRouteEdgePointClicked(oktId)
                            onEdgePointClick.invoke(geoPoint)
                        }
                    )
                    oktRoutesList.setHasFixedSize(true)
                    oktRoutesList.adapter = oktRoutesAdapter
                }
                oktRoutesCloseButton.setOnClickListener { onCloseClick.invoke() }
            }

            oktRoutesAdapter?.submitList(oktRoutes)

            show()

            postMainDelayed(RECYCLERVIEW_SCROLL_DELAY) {
                scrollTo(selectedOktId)
            }
        }
    }

    private fun scrollTo(oktId: String) {
        oktRoutesAdapter?.let { adapter ->
            val index = adapter.indexOf(oktId)

            binding.oktRoutesList.smoothScrollToPosition(index)
        }
    }

}

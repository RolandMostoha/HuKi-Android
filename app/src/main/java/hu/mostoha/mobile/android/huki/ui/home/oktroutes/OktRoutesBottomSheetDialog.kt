package hu.mostoha.mobile.android.huki.ui.home.oktroutes

import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.LayoutBottomSheetOktRoutesBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.postMainDelayed
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.OktType
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
    private var onDismiss: (() -> Unit)? = null
    private var isDraggedByUser = false

    init {
        addStateListener { state ->
            when (state) {
                BottomSheetBehavior.STATE_DRAGGING -> isDraggedByUser = true
                BottomSheetBehavior.STATE_HIDDEN -> {
                    if (isDraggedByUser) onDismiss?.invoke()
                    isDraggedByUser = false
                }
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_EXPANDED -> isDraggedByUser = false
            }
        }
    }

    override fun updateInset(insets: Insets) {
        binding.oktRoutesList.updatePadding(
            bottom = insets.bottom + resources.getDimensionPixelSize(R.dimen.space_large)
        )
    }

    fun init(
        oktType: OktType,
        oktRoutes: List<OktRouteUiModel>,
        selectedOktId: String,
        onRouteClick: (String) -> Unit,
        onStartClick: (String) -> Unit,
        onReverseClick: (String) -> Unit,
        onStampClick: (GeoPoint) -> Unit,
        onCloseClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        this.onDismiss = onDismiss

        postMain {
            with(binding) {
                oktRoutesHeaderContainer.headerImage.gone()
                oktRoutesHeaderContainer.headerStartButton.visible()
                oktRoutesHeaderContainer.headerStartButton.contentDescription =
                    context.getString(R.string.okt_routes_menu_action_start)
                oktRoutesHeaderContainer.headerStartButton.setOnClickListener {
                    analyticsService.oktRouteStartClicked(selectedOktId)
                    onStartClick.invoke(selectedOktId)
                }
                oktRoutesHeaderContainer.headerTitle.text = when (oktType) {
                    OktType.OKT -> context.getString(R.string.okt_okt_title)
                    OktType.RPDDK -> context.getString(R.string.okt_rpddk_title)
                    OktType.AKT -> context.getString(R.string.okt_akt_title)
                }
                oktRoutesHeaderContainer.headerSubTitle.text = when (oktType) {
                    OktType.OKT -> context.getString(R.string.okt_okt_subtitle)
                    OktType.RPDDK -> context.getString(R.string.okt_rpddk_subtitle_long)
                    OktType.AKT -> context.getString(R.string.okt_akt_subtitle)
                }
                oktRoutesHeaderContainer.headerCloseButton.setOnClickListener { onCloseClick.invoke() }

                if (oktRoutesAdapter == null) {
                    oktRoutesAdapter = OktRoutesAdapter(
                        onItemClick = { oktId ->
                            analyticsService.oktRouteClicked(oktId)
                            onRouteClick.invoke(oktId)
                        },
                        onLinkClick = { oktId, link ->
                            analyticsService.oktRouteLinkClicked(oktId)
                            context.openUrl(link)
                        },
                        onStartClick = { oktId ->
                            analyticsService.oktRouteStartClicked(oktId)
                            onStartClick.invoke(oktId)
                        },
                        onReverseClick = { oktId ->
                            analyticsService.oktRouteReverseClicked(oktId)
                            onReverseClick.invoke(oktId)
                        },
                        onStampClick = { oktId, stamp ->
                            analyticsService.oktStampClicked(oktId)
                            onStampClick.invoke(stamp.geoPoint)
                        },
                    )
                    oktRoutesList.setHasFixedSize(true)
                    (oktRoutesList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
                    oktRoutesList.adapter = oktRoutesAdapter
                }
            }

            oktRoutesAdapter?.submitList(oktRoutes)

            show()

            postMainDelayed(RECYCLERVIEW_SCROLL_DELAY) {
                scrollTo(selectedOktId)
            }
        }
    }

    fun selectStamp(oktId: String, geoPoint: GeoPoint) {
        val index = oktRoutesAdapter?.indexOf(oktId) ?: return
        val viewHolder = binding.oktRoutesList.findViewHolderForAdapterPosition(index)
            as? OktRoutesAdapter.ViewHolderItem

        if (viewHolder != null) {
            viewHolder.selectStamp(geoPoint)
        } else {
            binding.oktRoutesList.scrollToPosition(index)
            postMainDelayed(RECYCLERVIEW_SCROLL_DELAY) {
                val scrolledViewHolder = binding.oktRoutesList.findViewHolderForAdapterPosition(index)
                    as? OktRoutesAdapter.ViewHolderItem
                scrolledViewHolder?.selectStamp(geoPoint)
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

package hu.mostoha.mobile.android.huki.ui.home.oktroutes

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemOktRoutesBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuActionItem
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback
import org.osmdroid.util.GeoPoint

class OktRoutesAdapter(
    val onItemClick: (String) -> Unit,
    val onLinkClick: (String, String) -> Unit,
    val onEdgePointClick: (String, GeoPoint) -> Unit,
) : ListAdapter<OktRouteUiModel, RecyclerView.ViewHolder>(DefaultDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderItem(ItemOktRoutesBinding.inflate(parent.context.inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderItem -> {
                holder.bind((getItem(position) as OktRouteUiModel))
            }
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemOktRoutesBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(oktRouteUiModel: OktRouteUiModel) {
            with(binding) {
                if (oktRouteUiModel.isSelected) {
                    oktRoutesItemContainer.isSelected = true
                    oktRoutesItemContainer.setBackgroundResource(R.drawable.background_okt_routes_selected)
                } else {
                    oktRoutesItemContainer.isSelected = false
                    oktRoutesItemContainer.setBackgroundResource(R.color.colorBackground)
                }
                oktRoutesItemContainer.setOnClickListener {
                    onItemClick.invoke(oktRouteUiModel.oktId)
                }
                oktRoutesItemTitle.text = oktRouteUiModel.routeName
                oktRoutesItemNumber.visibleOrGone(oktRouteUiModel.routeNumber.isNotBlank())
                oktRoutesItemNumberPrefix.text = oktRouteUiModel.oktId.split("-").firstOrNull()
                routeAttributesTimeText.setMessage(oktRouteUiModel.travelTimeText)
                routeAttributesDistanceText.setMessage(oktRouteUiModel.distanceText)
                routeAttributesUphillText.setMessage(oktRouteUiModel.inclineText)
                routeAttributesDownhillText.setMessage(oktRouteUiModel.declineText)
                oktRoutesItemNumber.text = oktRouteUiModel.routeNumber
                oktRoutesItemActionsButton.contentDescription = binding.root.context.getString(
                    R.string.accessibility_okt_routes_action_button,
                    oktRouteUiModel.oktId
                )
                oktRoutesItemActionsButton.setOnClickListener {
                    showActionsPopupMenu(oktRoutesItemActionsButton, oktRouteUiModel)
                }
            }
        }
    }

    fun indexOf(oktId: String): Int {
        return currentList.indexOfFirst { it.oktId == oktId }
    }

    private fun showActionsPopupMenu(anchorView: View, oktRouteUiModel: OktRouteUiModel) {
        anchorView.context.showPopupMenu(
            anchorView = anchorView,
            actionItems = listOf(
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.okt_routes_menu_action_details,
                        startIconId = R.drawable.ic_okt_routes_action_details
                    ),
                    onClick = {
                        onLinkClick.invoke(oktRouteUiModel.oktId, oktRouteUiModel.detailsUrl)
                    }
                ),
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.okt_routes_menu_action_start_point,
                        startIconId = R.drawable.ic_popup_menu_google_maps_start
                    ),
                    onClick = {
                        onEdgePointClick.invoke(
                            oktRouteUiModel.oktId,
                            oktRouteUiModel.start
                        )
                    }
                ),
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.okt_routes_menu_action_end_point,
                        startIconId = R.drawable.ic_popup_menu_google_maps_end
                    ),
                    onClick = {
                        onEdgePointClick.invoke(
                            oktRouteUiModel.oktId,
                            oktRouteUiModel.end
                        )
                    }
                ),
            ),
            width = R.dimen.default_popup_menu_width_with_header,
            showAtCenter = true,
            headerTitle = oktRouteUiModel.routeName.toMessage(),
        )
    }

}

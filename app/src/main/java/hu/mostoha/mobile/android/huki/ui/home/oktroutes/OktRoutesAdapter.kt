package hu.mostoha.mobile.android.huki.ui.home.oktroutes

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
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class OktRoutesAdapter(
    val onItemClick: (String) -> Unit,
    val onLinkClick: (String, String) -> Unit,
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
            val context = binding.root.context

            with(binding) {
                if (oktRouteUiModel.isSelected) {
                    oktRoutesItemContainer.isSelected = true
                    oktRoutesItemContainer.setBackgroundResource(R.drawable.background_okt_routes_selected)
                } else {
                    oktRoutesItemContainer.isSelected = false
                    oktRoutesItemContainer.setBackgroundResource(R.color.transparent)
                }
                oktRoutesItemContainer.setOnClickListener {
                    onItemClick.invoke(oktRouteUiModel.id)
                }
                oktRoutesItemTitle.text = oktRouteUiModel.routeName
                oktRoutesItemNumber.visibleOrGone(oktRouteUiModel.routeNumber.isNotBlank())
                routeAttributesTimeText.setMessage(oktRouteUiModel.travelTimeText)
                routeAttributesDistanceText.setMessage(oktRouteUiModel.distanceText)
                routeAttributesUphillText.setMessage(oktRouteUiModel.inclineText)
                routeAttributesDownhillText.setMessage(oktRouteUiModel.declineText)
                oktRoutesItemNumber.text = oktRouteUiModel.routeNumber
                oktRoutesItemActionsButton.setOnClickListener {
                    context.showPopupMenu(
                        oktRoutesItemActionsButton,
                        listOf(
                            PopupMenuActionItem(
                                popupMenuItem = PopupMenuItem(
                                    R.string.okt_routes_menu_action_details,
                                    R.drawable.ic_okt_routes_action_details
                                ),
                                onClick = {
                                    onLinkClick.invoke(oktRouteUiModel.id, oktRouteUiModel.detailsUrl)
                                }
                            ),
                        )
                    )
                }
            }
        }
    }

    fun indexOf(oktId: String): Int {
        return currentList.indexOfFirst { it.id == oktId }
    }

}

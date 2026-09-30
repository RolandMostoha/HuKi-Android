package hu.mostoha.mobile.android.huki.ui.home.oktroutes

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
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
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktStampUiModel
import hu.mostoha.mobile.android.huki.model.ui.displayedStamps
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import org.osmdroid.util.GeoPoint

class OktRoutesAdapter(
    val onItemClick: (String) -> Unit,
    val onLinkClick: (String, String) -> Unit,
    val onStartClick: (String) -> Unit,
    val onReverseClick: (String) -> Unit,
    val onStampClick: (String, OktStampUiModel) -> Unit,
) : ListAdapter<OktRouteUiModel, RecyclerView.ViewHolder>(OktRouteDiffCallback) {

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

        private var boundOktId: String? = null

        private val stampsAdapter = OktStampsAdapter(
            onStampClick = { stamp -> boundOktId?.let { onStampClick.invoke(it, stamp) } }
        ).also { binding.oktRoutesItemStampsList.adapter = it }

        init {
            binding.oktRoutesItemContainer.clipToOutline = true
        }

        fun bind(oktRouteUiModel: OktRouteUiModel) {
            boundOktId = oktRouteUiModel.oktId
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
                val reversedIcon = if (oktRouteUiModel.isReversed) {
                    ContextCompat.getDrawable(root.context, R.drawable.ic_okt_routes_reversed)?.mutate()?.apply {
                        val tintColor = if (oktRouteUiModel.isSelected) {
                            R.color.colorOnPrimary
                        } else {
                            R.color.colorPrimaryText
                        }
                        setTint(ContextCompat.getColor(root.context, tintColor))
                    }
                } else {
                    null
                }
                oktRoutesItemTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(reversedIcon, null, null, null)
                oktRoutesItemTitle.contentDescription = if (oktRouteUiModel.isReversed) {
                    root.context.getString(
                        R.string.accessibility_okt_routes_reversed_title,
                        oktRouteUiModel.routeName
                    )
                } else {
                    null
                }
                oktRoutesItemNumber.visibleOrGone(oktRouteUiModel.routeNumber.isNotBlank())
                oktRoutesItemNumberPrefix.text = oktRouteUiModel.oktId.split("-").firstOrNull()
                routeAttributesTimeText.setMessage(oktRouteUiModel.travelTimeText)
                routeAttributesDistanceText.setMessage(oktRouteUiModel.distanceText)
                routeAttributesUphillText.setMessage(oktRouteUiModel.inclineText)
                routeAttributesDownhillText.setMessage(oktRouteUiModel.declineText)
                oktRoutesItemNumber.text = oktRouteUiModel.routeNumber
                val stamps = oktRouteUiModel.displayedStamps
                val showStamps = oktRouteUiModel.isSelected && stamps.isNotEmpty()
                oktRoutesItemStampsList.visibleOrGone(showStamps)
                stampsAdapter.submitStamps(if (showStamps) stamps else emptyList())
                oktRoutesItemActionsButton.contentDescription = binding.root.context.getString(
                    R.string.accessibility_okt_routes_action_button,
                    oktRouteUiModel.oktId
                )
                oktRoutesItemActionsButton.setOnClickListener {
                    showActionsPopupMenu(oktRoutesItemActionsButton, oktRouteUiModel)
                }
            }
        }

        fun selectStamp(geoPoint: GeoPoint) {
            val index = stampsAdapter.selectStamp(geoPoint)
            if (index != -1) {
                binding.oktRoutesItemStampsList.smoothScrollToPosition(index)
            }
        }
    }

    fun indexOf(oktId: String): Int {
        return currentList.indexOfFirst { it.oktId == oktId }
    }

    private fun showActionsPopupMenu(anchorView: View, oktRouteUiModel: OktRouteUiModel) {
        val detailsAction = PopupMenuActionItem(
            popupMenuItem = PopupMenuItem(
                title = R.string.okt_routes_menu_action_details.toMessage(),
                subTitle = R.string.okt_routes_menu_action_details_subtitle.toMessage(),
                startIconId = R.drawable.ic_okt_routes_action_details
            ),
            onClick = {
                onLinkClick.invoke(oktRouteUiModel.oktId, oktRouteUiModel.detailsUrl)
            }
        )
        val startAction = PopupMenuActionItem(
            popupMenuItem = PopupMenuItem(
                title = R.string.okt_routes_menu_action_start.toMessage(),
                subTitle = Message.Res(
                    R.string.okt_routes_menu_action_start_subtitle,
                    listOf(oktRouteUiModel.routeName)
                ),
                startIconId = R.drawable.ic_popup_menu_google_maps_start
            ),
            onClick = {
                onStartClick.invoke(oktRouteUiModel.oktId)
            }
        )
        val reverseAction = PopupMenuActionItem(
            popupMenuItem = PopupMenuItem(
                title = R.string.okt_routes_menu_action_reverse.toMessage(),
                subTitle = R.string.okt_routes_menu_action_reverse_subtitle.toMessage(),
                startIconId = R.drawable.ic_swap_horiz
            ),
            onClick = {
                onReverseClick.invoke(oktRouteUiModel.oktId)
            }
        )

        anchorView.context.showPopupMenu(
            anchorView = anchorView,
            actionItems = listOfNotNull(
                detailsAction,
                startAction,
                reverseAction.takeIf { oktRouteUiModel.stamps.isNotEmpty() },
            ),
            width = R.dimen.default_popup_menu_width_with_header,
            showAtCenter = true,
            headerTitle = oktRouteUiModel.routeName.toMessage(),
        )
    }

}

private object OktRouteDiffCallback : DiffUtil.ItemCallback<OktRouteUiModel>() {

    override fun areItemsTheSame(oldItem: OktRouteUiModel, newItem: OktRouteUiModel): Boolean {
        return oldItem.oktId == newItem.oktId
    }

    override fun areContentsTheSame(oldItem: OktRouteUiModel, newItem: OktRouteUiModel): Boolean {
        return oldItem == newItem
    }

}

package hu.mostoha.mobile.android.huki.ui.home.history.place

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemHistoryHeaderBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHistoryInfoBinding
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceHistoryBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuActionItem
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setDrawableTop
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class PlaceHistoryAdapter(
    val onPlaceOpen: (PlaceHistoryAdapterModel.Item) -> Unit,
    val onPlaceDelete: (PlaceHistoryAdapterModel.Item) -> Unit,
) : ListAdapter<PlaceHistoryAdapterModel, RecyclerView.ViewHolder>(
    DefaultDiffUtilCallback<PlaceHistoryAdapterModel>()
) {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_INFO = 1
        private const val TYPE_HEADER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.inflater

        return when (viewType) {
            TYPE_ITEM -> {
                ViewHolderItem(ItemPlaceHistoryBinding.inflate(inflater, parent, false))
            }
            TYPE_HEADER -> {
                ViewHolderHeader(ItemHistoryHeaderBinding.inflate(inflater, parent, false))
            }
            TYPE_INFO -> {
                ViewHolderEmpty(ItemHistoryInfoBinding.inflate(inflater, parent, false))
            }
            else -> throw IllegalArgumentException("Not supported viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderItem -> {
                holder.bind((getItem(position) as PlaceHistoryAdapterModel.Item))
            }
            is ViewHolderHeader -> {
                holder.bind((getItem(position) as PlaceHistoryAdapterModel.Header))
            }
            is ViewHolderEmpty -> {
                holder.bind((getItem(position) as PlaceHistoryAdapterModel.InfoView))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlaceHistoryAdapterModel.Item -> TYPE_ITEM
            is PlaceHistoryAdapterModel.Header -> TYPE_HEADER
            is PlaceHistoryAdapterModel.InfoView -> TYPE_INFO
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemPlaceHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaceHistoryAdapterModel.Item) {
            val context = binding.root.context
            val placeUiModel = item.placeUiModel

            with(binding) {
                placeHistoryItemName.text = placeUiModel.primaryText.resolve(context)
                placeHistoryAddressText.text = placeUiModel.secondaryText.resolve(context)
                bindFeatureIcon(placeUiModel.placeFeature, context)
                placeHistoryActionsButton.setOnClickListener {
                    context.showPopupMenu(
                        placeHistoryActionsButton,
                        listOf(
                            PopupMenuActionItem(
                                popupMenuItem = PopupMenuItem(
                                    titleId = R.string.place_history_menu_action_delete,
                                    startIconId = R.drawable.ic_gpx_history_action_delete
                                ),
                                onClick = { onPlaceDelete.invoke(item) }
                            ),
                        )
                    )
                }
                placeHistoryCardContainer.setOnClickListener {
                    onPlaceOpen.invoke(item)
                }
            }
        }

        private fun ItemPlaceHistoryBinding.bindFeatureIcon(placeFeature: PlaceFeature, context: Context) {
            if (placeFeature == PlaceFeature.OKT_WAYPOINT || placeFeature == PlaceFeature.GPX_WAYPOINT) {
                placeHistoryIcon.gone()
                placeHistoryTextIcon.visible()
            } else {
                placeHistoryIcon.visible()
                placeHistoryTextIcon.gone()
            }
            when (placeFeature) {
                PlaceFeature.MAP_SEARCH -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_history_place_finder)
                }
                PlaceFeature.MAP_MY_LOCATION -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_history_my_location)
                }
                PlaceFeature.MAP_PICKED_LOCATION -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_finder_pick_location)
                }
                PlaceFeature.ROUTE_PLANNER_SEARCH -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_history_route_planner)
                }
                PlaceFeature.ROUTE_PLANNER_MY_LOCATION -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_history_route_planner)
                }
                PlaceFeature.ROUTE_PLANNER_PICKED_LOCATION -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_history_route_planner)
                }
                PlaceFeature.HIKING_ROUTE_WAYPOINT -> {
                    placeHistoryIcon.setImageResource(R.drawable.ic_place_history_hiking_route)
                }
                PlaceFeature.OKT_WAYPOINT -> {
                    placeHistoryTextIcon.text = context.getString(R.string.place_history_icon_text_okt)
                }
                PlaceFeature.GPX_WAYPOINT -> {
                    placeHistoryTextIcon.text = context.getString(R.string.place_history_icon_text_gpx)
                }
            }
        }
    }

    inner class ViewHolderEmpty(
        private val binding: ItemHistoryInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(infoView: PlaceHistoryAdapterModel.InfoView) {
            with(binding) {
                historyItemInfoViewMessage.setText(infoView.message)
                historyItemInfoViewMessage.setDrawableTop(infoView.iconRes)
            }
        }
    }

    inner class ViewHolderHeader(
        private val binding: ItemHistoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaceHistoryAdapterModel.Header) {
            with(binding) {
                historyItemDateHeaderText.text = item.dateText.resolve(binding.root.context)
            }
        }
    }

}

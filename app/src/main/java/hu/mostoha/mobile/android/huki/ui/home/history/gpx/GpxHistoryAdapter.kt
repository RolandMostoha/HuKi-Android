package hu.mostoha.mobile.android.huki.ui.home.history.gpx

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemGpxHistoryBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHistoryInfoBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuActionItem
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setDrawableTop
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class GpxHistoryAdapter(
    val onGpxOpen: (GpxHistoryAdapterModel.Item) -> Unit,
    val onGpxShare: (GpxHistoryAdapterModel.Item) -> Unit,
    val onGpxDelete: (GpxHistoryAdapterModel.Item) -> Unit,
    val onGpxRename: (GpxHistoryAdapterModel.Item) -> Unit,
) : ListAdapter<GpxHistoryAdapterModel, RecyclerView.ViewHolder>(DefaultDiffUtilCallback<GpxHistoryAdapterModel>()) {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_INFO = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.inflater

        return when (viewType) {
            TYPE_ITEM -> {
                ViewHolderItem(ItemGpxHistoryBinding.inflate(parent.context.inflater, parent, false))
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
                holder.bind((getItem(position) as GpxHistoryAdapterModel.Item))
            }
            is ViewHolderEmpty -> {
                holder.bind((getItem(position) as GpxHistoryAdapterModel.InfoView))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GpxHistoryAdapterModel.Item -> TYPE_ITEM
            is GpxHistoryAdapterModel.InfoView -> TYPE_INFO
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemGpxHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GpxHistoryAdapterModel.Item) {
            val context = binding.root.context

            with(binding) {
                gpxHistoryItemName.text = item.name
                gpxHistoryActionsButton.setOnClickListener {
                    context.showPopupMenu(
                        gpxHistoryActionsButton,
                        listOf(
                            PopupMenuActionItem(
                                popupMenuItem = PopupMenuItem(
                                    R.string.gpx_history_menu_action_rename,
                                    R.drawable.ic_gpx_history_action_rename
                                ),
                                onClick = { onGpxRename.invoke(item) }
                            ),
                            PopupMenuActionItem(
                                popupMenuItem = PopupMenuItem(
                                    R.string.gpx_history_menu_action_delete,
                                    R.drawable.ic_gpx_history_action_delete
                                ),
                                onClick = { onGpxDelete.invoke(item) }
                            ),
                        )
                    )
                }
                gpxHistoryItemOpenButton.setOnClickListener {
                    onGpxOpen.invoke(item)
                }
                gpxHistoryItemShareButton.setOnClickListener {
                    onGpxShare.invoke(item)
                }
                gpxHistoryDateText.text = item.dateText.resolve(root.context)
            }
        }
    }

    inner class ViewHolderEmpty(
        private val binding: ItemHistoryInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(infoView: GpxHistoryAdapterModel.InfoView) {
            with(binding) {
                historyItemInfoViewMessage.setText(infoView.message)
                historyItemInfoViewMessage.setDrawableTop(infoView.iconRes)
            }
        }
    }

}
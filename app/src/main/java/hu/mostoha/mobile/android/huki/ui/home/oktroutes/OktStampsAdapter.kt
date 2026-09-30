package hu.mostoha.mobile.android.huki.ui.home.oktroutes

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.databinding.ItemOktStampBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setMessage
import hu.mostoha.mobile.android.huki.model.ui.OktStampUiModel
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback
import org.osmdroid.util.GeoPoint

class OktStampsAdapter(
    val onStampClick: (OktStampUiModel) -> Unit,
) : ListAdapter<OktStampUiModel, OktStampsAdapter.ViewHolderItem>(DefaultDiffUtilCallback()) {

    private var selectedStamp: OktStampUiModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItem {
        return ViewHolderItem(ItemOktStampBinding.inflate(parent.context.inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderItem, position: Int) {
        holder.bind(getItem(position))
    }

    fun submitStamps(stamps: List<OktStampUiModel>) {
        if (stamps != currentList) {
            selectedStamp = null
        }
        submitList(stamps)
    }

    inner class ViewHolderItem(
        private val binding: ItemOktStampBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stamp: OktStampUiModel) {
            with(binding) {
                oktStampItemContainer.isSelected = stamp == selectedStamp
                oktStampItemDistance.setMessage(stamp.distanceText)
                oktStampItemTitle.text = stamp.title
                oktStampItemTag.text = stamp.stampTag
                oktStampItemContainer.setOnClickListener {
                    selectStamp(stamp)
                    onStampClick.invoke(stamp)
                }
            }
        }
    }

    fun selectStamp(geoPoint: GeoPoint): Int {
        val stamp = currentList.firstOrNull { it.geoPoint == geoPoint } ?: return -1
        selectStamp(stamp)
        return currentList.indexOf(stamp)
    }

    private fun selectStamp(stamp: OktStampUiModel) {
        val previousIndex = currentList.indexOf(selectedStamp)
        selectedStamp = stamp
        if (previousIndex != -1) {
            notifyItemChanged(previousIndex)
        }
        notifyItemChanged(currentList.indexOf(stamp))
    }

}

package hu.mostoha.mobile.android.huki.ui.home.placefinder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceFinderPlaceBinding
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceFinderShowMoreHistoryBinding
import hu.mostoha.mobile.android.huki.databinding.ItemPlaceFinderStaticActionsBinding
import hu.mostoha.mobile.android.huki.databinding.ViewErrorBinding
import hu.mostoha.mobile.android.huki.databinding.ViewLoadingIndicatorBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setDrawableTop
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.model.ui.resolve

class PlaceFinderAdapter(
    context: Context,
    private val onMyLocationClick: () -> Unit,
    private val onManualLocationClick: () -> Unit,
    private val onShowMoreHistoryClick: (() -> Unit)? = null,
) : ArrayAdapter<PlaceFinderItem>(context, 0) {

    private lateinit var itemList: List<PlaceFinderItem>

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val placeFinderItem = getItem(position)!!

        val holder: ViewHolder
        val view: View

        when (placeFinderItem) {
            is PlaceFinderItem.Place -> {
                if (convertView == null || convertView.tag == null) {
                    val binding = ItemPlaceFinderPlaceBinding.inflate(parent.context.inflater, parent, false)
                    view = binding.root
                    holder = ViewHolder()
                    holder.primaryText = binding.placeFinderPrimaryText
                    holder.secondaryText = binding.placeFinderSecondaryText
                    holder.iconImage = binding.placeFinderIcon
                    holder.distanceText = binding.placeFinderDistanceText
                    binding.root.tag = holder
                } else {
                    view = convertView
                    holder = convertView.tag as ViewHolder
                }

                holder.primaryText.text = placeFinderItem.placeUiModel.primaryText.resolve(context)
                holder.secondaryText.setMessageOrGone(placeFinderItem.placeUiModel.secondaryText)
                holder.iconImage.setImageResource(placeFinderItem.placeUiModel.iconRes)
                holder.distanceText.setMessageOrGone(placeFinderItem.placeUiModel.distanceText)
            }
            is PlaceFinderItem.StaticActions -> {
                val binding = ItemPlaceFinderStaticActionsBinding.inflate(parent.context.inflater, parent, false)
                binding.placeFinderMyLocationButton.setOnClickListener {
                    onMyLocationClick.invoke()
                }
                binding.placeFinderPickLocationButton.setOnClickListener {
                    onManualLocationClick.invoke()
                }

                view = binding.root
            }
            is PlaceFinderItem.ShowMoreHistory -> {
                val binding = ItemPlaceFinderShowMoreHistoryBinding.inflate(parent.context.inflater, parent, false)

                if (onShowMoreHistoryClick != null) {
                    binding.placeFinderShowMoreHistoryButton.setOnClickListener {
                        onShowMoreHistoryClick.invoke()
                    }
                }

                view = binding.root
            }
            is PlaceFinderItem.Loading -> {
                val binding = ViewLoadingIndicatorBinding.inflate(parent.context.inflater, parent, false)

                view = binding.root
            }
            is PlaceFinderItem.Info -> {
                val binding = ViewErrorBinding.inflate(parent.context.inflater, parent, false)
                binding.errorViewText.text = parent.context.getString(placeFinderItem.messageRes.res)
                binding.errorViewText.setDrawableTop(placeFinderItem.drawableRes)

                view = binding.root
            }
        }

        return view
    }

    fun submitList(results: List<PlaceFinderItem>) {
        itemList = results

        clear()

        addAll(itemList)
    }

    inner class ViewHolder {
        lateinit var primaryText: TextView
        lateinit var secondaryText: TextView
        lateinit var iconImage: ImageView
        lateinit var distanceText: TextView
    }

}

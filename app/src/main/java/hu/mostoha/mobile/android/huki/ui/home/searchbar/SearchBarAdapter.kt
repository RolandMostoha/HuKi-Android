package hu.mostoha.mobile.android.huki.ui.home.searchbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hu.mostoha.mobile.android.huki.databinding.ItemHomeSearchBarInfoBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHomeSearchBarPlaceBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setDrawableTop
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone

class SearchBarAdapter(context: Context) : ArrayAdapter<SearchBarItem>(context, 0) {

    private lateinit var itemList: List<SearchBarItem>

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val searchBarItem = getItem(position)!!

        val holder: ViewHolder
        val view: View

        when (searchBarItem) {
            is SearchBarItem.Place -> {
                if (convertView == null || convertView.tag == null) {
                    val binding = ItemHomeSearchBarPlaceBinding.inflate(parent.context.inflater, parent, false)
                    view = binding.root
                    holder = ViewHolder()
                    holder.primaryText = binding.searchBarResultPrimaryText
                    holder.secondaryText = binding.searchBarResultSecondaryText
                    holder.iconImage = binding.homeSearchBarResultIcon
                    binding.root.tag = holder
                } else {
                    view = convertView
                    holder = convertView.tag as ViewHolder
                }

                holder.primaryText.text = searchBarItem.placeUiModel.primaryText
                holder.secondaryText.setMessageOrGone(searchBarItem.placeUiModel.secondaryText)
                holder.iconImage.setImageResource(searchBarItem.placeUiModel.iconRes)
            }
            is SearchBarItem.Info -> {
                val binding = ItemHomeSearchBarInfoBinding.inflate(parent.context.inflater, parent, false)
                binding.searchBarInfoView.text = parent.context.getString(searchBarItem.messageRes)
                binding.searchBarInfoView.setDrawableTop(searchBarItem.drawableRes)

                view = binding.root
            }
        }

        return view
    }

    fun submitList(results: List<SearchBarItem>) {
        itemList = results

        clear()
        addAll(itemList)
    }

    inner class ViewHolder {
        lateinit var primaryText: TextView
        lateinit var secondaryText: TextView
        lateinit var iconImage: ImageView
    }

}

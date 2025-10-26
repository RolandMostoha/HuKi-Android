package hu.mostoha.mobile.android.huki.ui.home.landscapes

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemLandscapesBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.invisible
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class LandscapesAdapter(
    val onItemClick: (LandscapeUiModel) -> Unit,
    val onDetailsClick: (LandscapeUiModel) -> Unit,
) : ListAdapter<LandscapeDetailsUiModel, RecyclerView.ViewHolder>(DefaultDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderItem(ItemLandscapesBinding.inflate(parent.context.inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderItem -> {
                holder.bind((getItem(position) as LandscapeDetailsUiModel))
            }
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemLandscapesBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uiModel: LandscapeDetailsUiModel) {
            with(binding) {
                val context = root.context
                val landscapeColor = uiModel.landscapeUiModel.color.color(context)

                if (uiModel.isSelected) {
                    landscapesItemContainer.isSelected = true
                    landscapesItemContainer.setCardBackgroundColor(landscapeColor)
                    landscapesItemImage.imageTintList = landscapeColor.colorStateList()
                    landscapesItemDetailsButton.setTextColor(landscapeColor.colorStateList())
                    landscapesItemDetailsButton.visible()
                } else {
                    landscapesItemContainer.isSelected = false
                    landscapesItemContainer.setCardBackgroundColor(R.color.transparent.color(context))
                    landscapesItemImage.imageTintList = R.color.colorPrimaryIcon.color(context).colorStateList()
                    landscapesItemDetailsButton.invisible()
                }
                landscapesItemContainer.setOnClickListener {
                    onItemClick.invoke(uiModel.landscapeUiModel)
                }
                landscapesItemTitle.text = uiModel.landscapeUiModel.name.resolve(binding.root.context)
                landscapesItemImage.setImageResource(uiModel.landscapeUiModel.iconRes)

                val description = uiModel.landscapeUiModel.destinations.joinToString(", ") { it.name }
                landscapesItemDescription.text = description

                landscapesItemDetailsButton.setOnClickListener {
                    onDetailsClick.invoke(uiModel.landscapeUiModel)
                }
            }
        }
    }

    fun indexOf(osmId: String): Int {
        return currentList.indexOfFirst { it.landscapeUiModel.osmId == osmId }
    }

}

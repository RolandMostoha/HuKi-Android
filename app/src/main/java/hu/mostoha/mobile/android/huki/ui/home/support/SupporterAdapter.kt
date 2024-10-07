package hu.mostoha.mobile.android.huki.ui.home.support

import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import androidx.core.text.parseAsHtml
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemSupporterBinding
import hu.mostoha.mobile.android.huki.extensions.color
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.model.domain.BillingProductType
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.util.productBackgroundColor
import hu.mostoha.mobile.android.huki.util.productHighlightColor
import hu.mostoha.mobile.android.huki.util.productIconColor
import hu.mostoha.mobile.android.huki.util.productStrongTextColor
import hu.mostoha.mobile.android.huki.util.productTextColor
import hu.mostoha.mobile.android.huki.views.DefaultDiffUtilCallback

class SupporterAdapter : ListAdapter<BillingProductType, RecyclerView.ViewHolder>(DefaultDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderItem(ItemSupporterBinding.inflate(parent.context.inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderItem -> {
                holder.bind((getItem(position) as BillingProductType))
            }
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemSupporterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(productType: BillingProductType) {
            with(binding) {
                val context = binding.root.context
                val productColor = context.color(productType.productColorRes)

                supporterBadgeImage.imageTintList = productColor.productIconColor(context).colorStateList()
                supporterBadgeTitle.setTextColor(productColor.productTextColor(context))
                binding.supporterBadgeTitle.text = context.getString(
                    R.string.support_supporter_title_template,
                    productType.productName.resolve(context)
                )
                supporterBadgeMessage.setTextColor(productColor.productTextColor(context))
                supporterBadgeMessage.text = productType.productMessage.resolve(context).parseAsHtml()
                supporterBadgeMessage.movementMethod = LinkMovementMethod.getInstance()
                supporterBadgeMessage.setLinkTextColor(
                    productType.productColorRes
                        .color(context)
                        .productStrongTextColor(context)
                )
                supporterBadgeImage.setImageResource(productType.productIcon)
                supporterCard.setCardBackgroundColor(productColor.productBackgroundColor(context))
                supporterCard.strokeColor = productColor.productHighlightColor(context)
            }
        }
    }

}

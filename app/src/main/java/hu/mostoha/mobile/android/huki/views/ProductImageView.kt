package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.setPadding
import com.google.android.material.imageview.ShapeableImageView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.BillingProductType
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.util.productBackgroundColor
import hu.mostoha.mobile.android.huki.util.productHighlightColor
import hu.mostoha.mobile.android.huki.util.productIconColor

class ProductImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    fun setProductIcon(productType: BillingProductType) {
        setStrokeWidthResource(R.dimen.default_highlighted_card_stroke_width)
        setPadding(resources.getDimensionPixelSize(R.dimen.default_highlighted_card_stroke_width))
        setBackgroundColor(
            productType.productColorRes
                .color(context)
                .productBackgroundColor(context)
        )
        imageTintList = productType.productColorRes
            .color(context)
            .productIconColor(context)
            .colorStateList()
        setImageResource(productType.productIcon)
        strokeColor = productType.productColorRes
            .color(context)
            .productHighlightColor(context)
            .colorStateList()
    }

    fun setAppIcon() {
        setStrokeWidthResource(R.dimen.default_highlighted_card_stroke_width)
        setPadding(resources.getDimensionPixelSize(R.dimen.default_highlighted_card_stroke_width))
        setBackgroundColor(R.color.colorPrimaryExtraLight.color(context))
        imageTintList = R.color.colorPrimary
            .color(context)
            .colorStateList()
        setImageResource(R.drawable.ic_home_fab_hike_mode)
        strokeColor = R.color.transparent
            .color(context)
            .colorStateList()
    }

}

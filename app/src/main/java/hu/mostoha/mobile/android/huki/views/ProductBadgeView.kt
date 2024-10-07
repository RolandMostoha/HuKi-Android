package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ViewProductBadgeBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.util.productBackgroundColor
import hu.mostoha.mobile.android.huki.util.productIconColor
import hu.mostoha.mobile.android.huki.util.productStrokeColor
import hu.mostoha.mobile.android.huki.util.productTextColor

class ProductBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewProductBadgeBinding.inflate(context.inflater, this)

    private val productBadgeCardView by lazy { binding.productBadgeCardView }
    private val productBadgeImage by lazy { binding.productBadgeImage }
    private val productBadgeTitle by lazy { binding.productBadgeTitle }
    private val productBadgeSubtitle by lazy { binding.productBadgeSubtitle }

    var badgeTitle: String? = null
        set(value) {
            field = value

            productBadgeTitle.setTextOrGone(value)

            invalidate()
        }

    var badgeSubtitle: String? = null
        set(value) {
            field = value

            productBadgeSubtitle.setTextOrGone(value)

            invalidate()
        }

    @DrawableRes
    var badgeIcon: Int? = null
        set(value) {
            field = value

            if (value != null) {
                productBadgeImage.setImageResource(value)
            }

            invalidate()
        }

    @ColorInt
    var productColor: Int? = null
        set(value) {
            field = value

            if (value != null) {
                val textColor = value.productTextColor(context)
                val strokeColor = value.productStrokeColor(context)
                val backgroundColor = value.productBackgroundColor(context)

                productBadgeCardView.setCardBackgroundColor(backgroundColor)
                productBadgeCardView.strokeColor = strokeColor
                productBadgeImage.imageTintList = value.productIconColor(context).colorStateList()
                productBadgeTitle.setTextColor(textColor)
                productBadgeSubtitle.setTextColor(textColor)
            }

            invalidate()
        }

    @ColorInt
    var badgeBackgroundColor: Int? = null
        set(value) {
            field = value

            if (value != null) {
                productBadgeCardView.setCardBackgroundColor(value)
            }

            invalidate()
        }

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.ProductBadgeView)) {
            val productColor = getColor(R.styleable.ProductBadgeView_badgeIconColor, Color.BLACK)
            val textColor = productColor.productTextColor(context)
            val strokeColor = productColor.productStrokeColor(context)
            val backgroundColor = productColor.productBackgroundColor(context)

            productBadgeCardView.radius = getDimensionPixelSize(
                R.styleable.ProductBadgeView_badgeCornerRadius,
                resources.getDimensionPixelSize(R.dimen.default_corner_size_button)
            ).toFloat()
            productBadgeCardView.setCardBackgroundColor(backgroundColor)
            productBadgeCardView.strokeColor = strokeColor
            productBadgeImage.imageTintList = productColor.productIconColor(context).colorStateList()
            productBadgeImage.setImageDrawable(getDrawable(R.styleable.ProductBadgeView_badgeIcon))
            productBadgeTitle.setTextColor(textColor)
            productBadgeSubtitle.setTextColor(textColor)

            recycle()
        }
    }

    override fun setOnClickListener(clickListener: OnClickListener?) {
        productBadgeCardView.setOnClickListener(clickListener)
    }

}

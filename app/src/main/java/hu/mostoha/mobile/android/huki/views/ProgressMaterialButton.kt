package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.getDrawableOrThrow
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.colorStateList

class ProgressMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    @ColorInt
    private val iconColor: Int
    private val iconDrawable: Drawable
    private val progressIndicatorDrawable: Drawable

    var disabled: Boolean = false
        set(value) {
            if (value) {
                icon = iconDrawable
                iconTint = context.colorStateList(R.color.colorPrimaryTextDisabled)
                isEnabled = false
            } else {
                icon = iconDrawable
                iconTint = ColorStateList.valueOf(iconColor)
                isEnabled = true
            }

            invalidate()

            field = value
        }

    var inProgress: Boolean = false
        set(value) {
            if (value) {
                icon = progressIndicatorDrawable
                isEnabled = false
            } else {
                icon = iconDrawable
                isEnabled = true
            }

            invalidate()

            field = value
        }

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.ProgressMaterialButton)) {
            iconDrawable = getDrawableOrThrow(R.styleable.ProgressMaterialButton_iconRes)
            progressIndicatorDrawable = createProgressDrawable(context)
            icon = iconDrawable
            iconColor = getColor(
                R.styleable.ProgressMaterialButton_iconColor,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
            iconTint = ColorStateList.valueOf(iconColor)

            recycle()
        }
    }

    private fun createProgressDrawable(context: Context): IndeterminateDrawable<CircularProgressIndicatorSpec> {
        val progressIndicatorSpec = CircularProgressIndicatorSpec(
            context,
            null,
            0,
            R.style.Widget_MaterialComponents_CircularProgressIndicator_ExtraSmall
        )
        progressIndicatorSpec.indicatorColors = intArrayOf(ContextCompat.getColor(context, R.color.colorPrimary))

        val circularDrawable = IndeterminateDrawable.createCircularDrawable(context, progressIndicatorSpec).apply {
            bounds = iconDrawable.bounds
            setVisible(true, true)
        }

        return circularDrawable
    }

}

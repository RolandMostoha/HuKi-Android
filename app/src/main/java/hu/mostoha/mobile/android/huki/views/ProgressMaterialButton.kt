package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.getDrawableOrThrow
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import hu.mostoha.mobile.android.huki.R

class ProgressMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private val iconDrawable: Drawable

    private val progressIndicatorDrawable: Drawable

    var inProgress: Boolean = false
        set(value) {
            icon = if (value) {
                progressIndicatorDrawable
            } else {
                iconDrawable
            }

            invalidate()

            field = value
        }

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.ProgressMaterialButton)) {
            iconDrawable = getDrawableOrThrow(R.styleable.ProgressMaterialButton_iconRes)
        }

        progressIndicatorDrawable = createProgressDrawable(context)
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
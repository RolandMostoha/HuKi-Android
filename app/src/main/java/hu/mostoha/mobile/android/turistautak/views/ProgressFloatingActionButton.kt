package hu.mostoha.mobile.android.turistautak.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.gone
import hu.mostoha.mobile.android.turistautak.extensions.visible
import kotlinx.android.synthetic.main.view_progress_floating_action_button.view.*

class ProgressFloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.view_progress_floating_action_button, this, true)

        with(context.obtainStyledAttributes(attrs, R.styleable.ProgressFloatingActionButton)) {
            fab.setImageDrawable(getDrawable(R.styleable.ProgressFloatingActionButton_iconSrc))

            val fabSize = getInt(
                R.styleable.ProgressFloatingActionButton_fabSize,
                FloatingActionButton.SIZE_AUTO
            )
            fab.size = fabSize

            val progressBarSize = resources.getDimensionPixelSize(
                when (fabSize) {
                    FloatingActionButton.SIZE_MINI -> R.dimen.progress_fab_progress_bar_size_mini
                    FloatingActionButton.SIZE_NORMAL -> R.dimen.progress_fab_progress_bar_size_normal
                    else -> 0
                }
            )
            progressBar.layoutParams.apply {
                height = progressBarSize
                width = progressBarSize
            }

            recycle()
        }
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visible()
        } else {
            progressBar.gone()
        }
    }
}

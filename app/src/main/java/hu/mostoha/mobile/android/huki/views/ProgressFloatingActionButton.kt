package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ViewProgressFloatingActionButtonBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.visible

class ProgressFloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewProgressFloatingActionButtonBinding.inflate(context.inflater, this)

    private val progressBar by lazy { binding.progressFabProgressBar }
    private val fab by lazy { binding.progressFabButton }

    var inProgress: Boolean = false
        set(value) {
            if (value) {
                progressBar.visible()
            } else {
                progressBar.gone()
            }
            field = value
        }

    init {
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

    override fun setOnClickListener(listener: OnClickListener?) {
        fab.setOnClickListener(listener)
    }

}

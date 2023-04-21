package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ViewInfoButtonBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

class InfoButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewInfoButtonBinding.inflate(context.inflater, this)

    private val infoButton by lazy { binding.infoButton }

    var onClick: (() -> Unit)? = null

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.InfoButtonView)) {
            val message = getString(R.styleable.InfoButtonView_messageRes)

            infoButton.setOnClickListener {
                val contentView = context.inflater.inflate(R.layout.view_info_button_popup, null)

                SimpleTooltip.Builder(context)
                    .anchorView(infoButton)
                    .contentView(contentView, R.id.infoButtonPopupMessageText)
                    .text(message)
                    .arrowColor(context.getColor(R.color.colorInfoPopupBackground))
                    .margin(0f)
                    .gravity(Gravity.TOP)
                    .transparentOverlay(true)
                    .build()
                    .show()

                onClick?.invoke()
            }

            recycle()
        }
    }

}

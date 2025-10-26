package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ViewInfoButtonBinding
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.updateDrawableEnd
import hu.mostoha.mobile.android.huki.extensions.visible
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip

class InfoButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewInfoButtonBinding.inflate(context.inflater, this)

    private val infoButton by lazy { binding.infoButton }

    var onOpen: (() -> Unit)? = null
    var onDismiss: (() -> Unit)? = null
    var onContentClick: (() -> Unit)? = null

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.InfoButtonView)) {
            val isContentClickable = onContentClick != null
            val message = getString(R.styleable.InfoButtonView_messageRes)

            infoButton.setOnClickListener {
                val contentView = context.inflater.inflate(R.layout.view_info_button_popup, this@InfoButtonView)
                val simpleTooltip = SimpleTooltip.Builder(context)
                    .anchorView(infoButton)
                    .contentView(contentView, R.id.infoButtonPopupMessageText)
                    .text(message)
                    .arrowColor(context.getColor(R.color.colorInfoPopupBackground))
                    .margin(0f)
                    .gravity(Gravity.TOP)
                    .transparentOverlay(true)
                    .onShowListener { onOpen?.invoke() }
                    .onDismissListener { onDismiss?.invoke() }
                    .dismissOnInsideTouch(!isContentClickable)
                    .focusable(true)
                    .build()

                val closButton = contentView.findViewById<TextView>(R.id.infoButtonPopupCloseButton)
                val messageText = contentView.findViewById<TextView>(R.id.infoButtonPopupMessageText)

                if (isContentClickable) {
                    messageText.updateDrawableEnd(null)
                    messageText.setOnClickListener {
                        onContentClick?.invoke()
                        simpleTooltip.dismiss()
                    }
                    closButton.visible()
                    closButton.setOnClickListener {
                        simpleTooltip.dismiss()
                    }
                } else {
                    messageText.updateDrawableEnd(R.drawable.ic_info_popup_close)
                    closButton.gone()
                }

                simpleTooltip.show()
            }

            recycle()
        }
    }

}

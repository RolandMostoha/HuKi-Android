package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ViewStrokedTextViewBinding
import hu.mostoha.mobile.android.huki.extensions.inflater

class StrokedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_STROKE_WIDTH = 10f
        private const val DEFAULT_LETTER_SPACING = 0.025f
    }

    private val binding = ViewStrokedTextViewBinding.inflate(context.inflater, this)

    private val strokeTextView by lazy { binding.strokedTextViewStrokeText }
    private val textView by lazy { binding.strokedTextViewText }

    var text: String? = null
        set(value) {
            field = value

            strokeTextView.text = value
            textView.text = value

            invalidate()
        }

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.StrokedTextView)) {
            val textAppearance = getResourceId(R.styleable.StrokedTextView_textAppearance, 0)

            strokeTextView.setTextAppearance(textAppearance)
            strokeTextView.setTextColor(getColor(R.styleable.StrokedTextView_strokeColor, Color.WHITE))
            strokeTextView.paint.apply {
                strokeWidth = getDimension(R.styleable.StrokedTextView_strokeWidth, DEFAULT_STROKE_WIDTH)
                style = Paint.Style.STROKE
            }
            strokeTextView.letterSpacing = DEFAULT_LETTER_SPACING
            textView.setTextColor(getColor(R.styleable.StrokedTextView_textColor, Color.BLACK))
            textView.setTextAppearance(textAppearance)
            textView.letterSpacing = DEFAULT_LETTER_SPACING

            recycle()
        }
    }

}

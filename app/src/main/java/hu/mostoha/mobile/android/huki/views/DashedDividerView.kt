package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import hu.mostoha.mobile.android.huki.R

class DashedDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val DIRECTION_VERTICAL = 0
        const val DIRECTION_HORIZONTAL = 1
        const val DEFAULT_GAP = 5.25f
        const val DEFAULT_WIDTH = 5.25f
        const val DEFAULT_COLOR = Color.BLACK
    }

    private var dividerGap = 0f
    private var dividerWidth = 0f
    private var color = 0
    private var direction = DIRECTION_HORIZONTAL
    private val paint = Paint()
    private val path = Path()

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DashedDividerView,
            defStyleAttr,
            R.style.DefaultDashedDivider
        )

        with(typedArray) {
            dividerGap = getDimension(R.styleable.DashedDividerView_dashGap, DEFAULT_GAP)
            dividerWidth = getDimension(R.styleable.DashedDividerView_dashWidth, DEFAULT_WIDTH)
            color = getColor(R.styleable.DashedDividerView_dashColor, DEFAULT_COLOR)
            direction = getInt(R.styleable.DashedDividerView_dashDirection, DIRECTION_HORIZONTAL)

            paint.color = color
            paint.style = Paint.Style.STROKE
            paint.pathEffect = DashPathEffect(floatArrayOf(dividerWidth, dividerGap), 0f)
            paint.strokeWidth = dividerWidth

            recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.moveTo(0f, 0f)

        if (direction == DIRECTION_HORIZONTAL) {
            path.lineTo(measuredWidth.toFloat(), 0f)
        } else {
            path.lineTo(0f, measuredHeight.toFloat())
        }

        canvas.drawPath(path, paint)
    }

}

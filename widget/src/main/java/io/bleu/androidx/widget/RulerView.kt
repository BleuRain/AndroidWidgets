package io.bleu.androidx.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.VelocityTracker
import android.view.View
import android.widget.Scroller
import androidx.annotation.ColorInt
import io.bleu.androidx.widget.utils.Colors
import kotlin.math.roundToInt

class RulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint()
    private val textPaint = Paint()

    @ColorInt
    var startColor = Color.parseColor("#ff3415b0")

    @ColorInt
    var endColor = Color.parseColor("#ffcd0074")

    // Indicator
    private val indicatorRadius = 12f
    private val indicatorPaddingBottom = 30f
    private var indicatorCentralX = 0f
    private var indicatorCentralY = 0f

    // Lines
    private var lineY = 0f
    private val lineWidth = 24
    private val startNum = 0
    private val endNum = 40
    private val minLineHeight = 120f
    private val midLineHeight = 180f
    private val maxLineHeight = 240f
    private val linePaddingRight = 30
    private val linePaddingBottom = 20
    private val lineRadius = 12f

    // Text
    private val numTextSize = 100f
    private var textY = 0f

    private val scroller = Scroller(this.context)
    private var vw = 0
    private var vh = 0
    private var rulerWidth = 0f
    private var baseX = 0f
    private var downX = 0f
    private var offsetX = 0f
    private var deltaX = 0f
    private var velocityTracker = VelocityTracker.obtain()
    private var onValueChangedListener: OnValueChangedListener? = null
    private val currentValue = Int.MIN_VALUE

    init {
        textPaint.textSize = numTextSize
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        changeValue()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        vw = w
        vh = h
        baseX = (vw + lineWidth) / 2f

        indicatorCentralX = vw / 2f
        indicatorCentralY = indicatorRadius

        rulerWidth = (endNum - startNum) * (lineWidth + linePaddingRight).toFloat()
        lineY = indicatorRadius * 2 + indicatorPaddingBottom
        textY = lineY + maxLineHeight + linePaddingBottom + numTextSize
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event)
        when (event.actionMasked) {
            ACTION_DOWN -> {
                downX = event.x
                scroller.abortAnimation()
                deltaX = getRealOffsetX()
                offsetX = 0f
                return true
            }
            ACTION_MOVE -> {
                offsetX = event.x - downX
                changeValue()
                postInvalidate()
            }
            ACTION_UP -> {
                deltaX = getRealOffsetX()
                offsetX = 0f
                velocityTracker.computeCurrentVelocity(500)
                val velocity = velocityTracker.xVelocity.toInt()
                scroller.fling(0, 0, velocity, 0, Int.MIN_VALUE, Int.MAX_VALUE, 0, 0)
                val finalX = scroller.finalX
                scroller.finalX =
                    (finalX - ((finalX + deltaX) % (lineWidth + linePaddingRight))).toInt()
                postInvalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            offsetX = scroller.currX.toFloat()
            if (scroller.currX == scroller.finalX) {
                deltaX = getRealOffsetX()
                offsetX = 0f
            }
            changeValue()
            postInvalidate()
        }
    }

    private fun getRealOffsetX(): Float {
        return (offsetX + deltaX)
            .coerceAtMost(0f)
            .coerceAtLeast(-rulerWidth)
    }

    private fun changeValue() {
        onValueChangedListener?.apply {
            val currentValue = startNum - getRealOffsetX() / (lineWidth + linePaddingRight)
            onValueChanged(currentValue.roundToInt())
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        val radio = -getRealOffsetX() / rulerWidth
        paint.color = Colors.getColor(startColor, endColor, radio)
        canvas.drawCircle(indicatorCentralX, indicatorCentralY, indicatorRadius, paint)
    }

    private fun drawBarsAndNumbers(canvas: Canvas) {
        var startLineX = baseX + getRealOffsetX()
        for (num in startNum..endNum) {
            val height = when {
                num % 10 == 0 -> maxLineHeight
                num % 5 == 0 -> midLineHeight
                else -> minLineHeight
            }
            val currentColor = Colors.getColor(
                startColor,
                endColor,
                (num - startNum) / (endNum - startNum).toFloat()
            )
            paint.color = currentColor
            canvas.drawRoundRect(
                RectF(
                    startLineX - lineWidth,
                    lineY + height,
                    startLineX,
                    lineY
                ),
                lineRadius,
                lineRadius,
                paint
            )
            // 3. Draw numbers
            if (num % 10 == 0) {
                textPaint.color = currentColor
                drawNumberText(canvas, num, startLineX)
            }
            startLineX += lineWidth + linePaddingRight
        }
    }

    private fun drawNumberText(canvas: Canvas, num: Int, startLineX: Float) {
        textPaint.textSize = numTextSize
        val textWidth = textPaint.measureText("$num")
        canvas.drawText(
            num.toString(),
            startLineX - textWidth / 2 - lineWidth / 2,
            textY,
            textPaint
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw indicator circle
        drawIndicator(canvas)

        // 2. Draw bars & numbers
        drawBarsAndNumbers(canvas)
    }

    fun setOnValueChangedListener(listener: OnValueChangedListener) {
        onValueChangedListener = listener
    }

    interface OnValueChangedListener {
        fun onValueChanged(value: Int)
    }
}
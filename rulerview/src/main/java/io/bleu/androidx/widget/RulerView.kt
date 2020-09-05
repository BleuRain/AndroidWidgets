package io.bleu.androidx.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
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

    private companion object {
        private const val DefStartColor = "#ff3415b0"
        private const val DefEndColor = "#ffcd0074"

        private const val DefIndicatorRadius = 12f
        private const val DefIndicatorMarginBottom = 30f

        private const val DefStartValue = 0
        private const val DefEndValue = 40
        private const val DefLineSpace = 30
        private const val DefLineBoxMarginBottom = 20
        private const val DefLineRadius = 12f

        private const val DefValueTextSize = 100f
        private const val DefVelocityX = 1000
    }

    private val gestureDetector =
        GestureDetector(getContext(), object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent?) = true

            override fun onShowPress(e: MotionEvent?) = Unit

            override fun onSingleTapUp(e: MotionEvent?) = performClick()

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ) = false

            override fun onLongPress(e: MotionEvent?) = Unit

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) = false
        })
    private val paint = Paint()
    private val textPaint = Paint()

    @ColorInt
    private var startColor = Color.parseColor(DefStartColor)

    @ColorInt
    private var endColor = Color.parseColor(DefEndColor)

    // Indicator
    private val indicatorRadius = DefIndicatorRadius
    private val indicatorMarginBottom = DefIndicatorMarginBottom
    private var indicatorCentralX = 0f
    private var indicatorCentralY = 0f

    // Lines
    private var lineY = 0f
    private val lineWidth = 24
    private val startValue = DefStartValue
    private val endValue = DefEndValue
    private val minLineHeight = 120f
    private val midLineHeight = 180f
    private val maxLineHeight = 240f
    private val lineSpace = DefLineSpace
    private val lineBoxMarginBottom = DefLineBoxMarginBottom
    private val lineRadius = DefLineRadius

    // Text
    private val valueTextSize = DefValueTextSize
    private var textY = 0f
    private val textHeight: Float

    private val scroller = Scroller(this.context)
    private var velocityTracker = VelocityTracker.obtain()
    private var onValueChangedListener: OnValueChangedListener? = null
    private var currentValue = Int.MIN_VALUE

    private var viewWidth = 0
    private var viewHeight = 0
    private var rulerWidth = 0f
    private var rulerHeight = 0f
    private var drawBaseX = 0f
    private var moveStartX = 0f
    private var moveOffset = 0f
    private var deltaX = 0f

    init {
        textPaint.textSize = valueTextSize
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textHeight = textPaint.fontMetrics.let { fontMetrics ->
            fontMetrics.descent - fontMetrics.ascent
        }
        rulerWidth = (endValue - startValue) * (lineWidth + lineSpace).toFloat()
        rulerHeight =
            indicatorRadius * 2 + indicatorMarginBottom + maxLineHeight + lineBoxMarginBottom + textHeight
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        changeValue()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        drawBaseX = (viewWidth + lineWidth) / 2f

        indicatorCentralX = viewWidth / 2f
        indicatorCentralY = indicatorRadius

        lineY = indicatorRadius * 2 + indicatorMarginBottom
        textY = lineY + maxLineHeight + lineBoxMarginBottom + valueTextSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST -> {
                height = rulerHeight.toInt().coerceAtMost(height)
            }
            MeasureSpec.EXACTLY -> {
                // Do nothing
            }
            MeasureSpec.UNSPECIFIED -> {
                height = rulerHeight.toInt()
            }
        }
        setMeasuredDimension(width, height)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        velocityTracker.addMovement(event)
        when (event.actionMasked) {
            ACTION_DOWN -> {
                // Reset all
                moveStartX = event.x
                scroller.abortAnimation()
                deltaX = getRealOffsetX()
                moveOffset = 0f
            }
            ACTION_MOVE -> {
                moveOffset = event.x - moveStartX
                changeValue()
                postInvalidate()
            }
            ACTION_UP -> {
                deltaX = getRealOffsetX()
                moveOffset = 0f
                velocityTracker.computeCurrentVelocity(500)
                val velocityX = velocityTracker.xVelocity.toInt()
                scroller.fling(
                    0,
                    0,
                    velocityX,
                    0,
                    Int.MIN_VALUE,
                    Int.MAX_VALUE,
                    0,
                    0,
                )
                computeScrollerFinalX()
                postInvalidate()
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            moveOffset = scroller.currX.toFloat()
            if (scroller.currX == scroller.finalX) {
                deltaX = getRealOffsetX()
                moveOffset = 0f
            }
            changeValue()
            postInvalidate()
        }
    }

    fun getCurrentValue(): Int {
        return currentValue
    }

    fun setCurrentValue(value: Int) {
        if (value != currentValue) {
            if (!scroller.isFinished) {
                scroller.abortAnimation()
            }
            scroller.fling(
                0,
                0,
                DefVelocityX,
                0,
                Int.MIN_VALUE,
                Int.MAX_VALUE,
                0,
                0,
            )
            scroller.finalX =
                -(value - currentValue) * (lineWidth + lineSpace) + deltaX.toInt() % (lineWidth + lineSpace)
            computeScrollerFinalX()
            postInvalidate()
        }
    }

    private fun computeScrollerFinalX() {
        val originalFinalX = scroller.finalX
        scroller.finalX =
            (originalFinalX - (originalFinalX + deltaX) % (lineWidth + lineSpace)).toInt()
    }

    private fun getRealOffsetX(): Float {
        return (moveOffset + deltaX)
            .coerceAtMost(0f)
            .coerceAtLeast(-rulerWidth)
    }

    private fun changeValue() {
        onValueChangedListener?.apply {
            val newValue =
                (startValue - getRealOffsetX() / (lineWidth + lineSpace)).roundToInt()
            if (currentValue != newValue) {
                currentValue = newValue
                onValueChanged(newValue)
            }
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        val radio = -getRealOffsetX() / rulerWidth
        paint.color = Colors.getColor(startColor, endColor, radio)
        canvas.drawCircle(indicatorCentralX, indicatorCentralY, indicatorRadius, paint)
    }

    private fun drawBarsAndNumbers(canvas: Canvas) {
        var startLineX = drawBaseX + getRealOffsetX()
        for (num in startValue..endValue) {
            val height = when {
                num % 10 == 0 -> maxLineHeight
                num % 5 == 0 -> midLineHeight
                else -> minLineHeight
            }
            val currentColor = Colors.getColor(
                startColor,
                endColor,
                (num - startValue) / (endValue - startValue).toFloat()
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
            startLineX += lineWidth + lineSpace
        }
    }

    private fun drawNumberText(canvas: Canvas, num: Int, startLineX: Float) {
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

    fun getIndicatorColor(): Int {
        return Colors.getColor(
            startColor,
            endColor,
            currentValue.toFloat() / (endValue - startValue)
        )
    }

    interface OnValueChangedListener {
        fun onValueChanged(value: Int)
    }
}
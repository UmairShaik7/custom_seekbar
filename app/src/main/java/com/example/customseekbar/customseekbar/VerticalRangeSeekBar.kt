package com.example.customseekbar.customseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import com.example.customseekbar.R
import com.example.customseekbar.customseekbar.Utils.compareFloat
import com.example.customseekbar.customseekbar.Utils.measureText
import com.example.customseekbar.customseekbar.Utils.parseFloat

open class VerticalRangeSeekBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : RangeSeekBar(context, attrs) {
    //text direction of VerticalRangeSeekBar. include indicator and tickMark
    //direction of VerticalRangeSeekBar

    /**
     * set VerticalRangeSeekBar Orientation
     * [.DIRECTION_LEFT]
     * [.DIRECTION_RIGHT]
     */
    var orientation = DIRECTION_LEFT
    /**
     * set tick mark text direction
     * [.TEXT_DIRECTION_VERTICAL]
     * [.TEXT_DIRECTION_HORIZONTAL]
     */
    private var tickMarkDirection = TEXT_DIRECTION_VERTICAL
    private var maxTickMarkWidth = 0
    private fun initAttrs(attrs: AttributeSet?) {
        try {
            val t =
                context.obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar)
            orientation = t.getInt(
                R.styleable.VerticalRangeSeekBar_rsb_orientation,
                DIRECTION_LEFT
            )
            tickMarkDirection = t.getInt(
                R.styleable.VerticalRangeSeekBar_rsb_tick_mark_orientation,
                TEXT_DIRECTION_VERTICAL
            )
            t.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSeekBar(attrs: AttributeSet?) {
        leftSeekBar = attrs?.let { VerticalSeekBar(this, it, true) }
        rightSeekBar = attrs?.let { VerticalSeekBar(this, it, false) }
        (rightSeekBar as VerticalSeekBar).isVisible = getSeekBarMode() != SEEK_BAR_MODE_SINGLE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */if (widthMode == MeasureSpec.EXACTLY) {
            widthSize = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        } else if (widthMode == MeasureSpec.AT_MOST && parent is ViewGroup
            && widthSize == ViewGroup.LayoutParams.MATCH_PARENT
        ) {
            widthSize = MeasureSpec.makeMeasureSpec(
                (parent as ViewGroup).measuredHeight,
                MeasureSpec.AT_MOST
            )
        } else {
            val heightNeeded: Int = if (gravity == Gravity.CENTER) {
                2 * progressTop + progressHeight
            } else {
                rawHeight.toInt()
            }
            widthSize = MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthSize + PADDING_RIGHT_SEEKBAR, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        if (orientation == DIRECTION_LEFT) {
            canvas.rotate(-90f)
            canvas.translate(-height.toFloat(), 0f)
        } else {
            canvas.rotate(90f)
            canvas.translate(0f, -width.toFloat())
        }
        super.onDraw(canvas)
    }

    override fun onDrawTickMark(
        canvas: Canvas,
        paint: Paint
    ) {
        if (tickMarkTextArray != null) {
            val arrayLength: Int = tickMarkTextArray!!.size
            val trickPartWidth = progressWidth / (arrayLength - 1)
            for (i in 0 until arrayLength) {
                val text2Draw = tickMarkTextArray!![i].toString()
                if (TextUtils.isEmpty(text2Draw)) continue
                paint.getTextBounds(text2Draw, 0, text2Draw.length, tickMarkTextRect)
                paint.color = tickMarkTextColor
                //平分显示
                var x: Float
                if (tickMarkMode == TRICK_MARK_MODE_OTHER) {
                    x = when (tickMarkGravity) {
                        TICK_MARK_GRAVITY_RIGHT -> {
                            progressLeft + i * trickPartWidth - tickMarkTextRect.width().toFloat()
                        }
                        TICK_MARK_GRAVITY_CENTER -> {
                            progressLeft + i * trickPartWidth - tickMarkTextRect.width() / 2f
                        }
                        else -> {
                            progressLeft + i * trickPartWidth.toFloat()
                        }
                    }
                } else {
                    val num =
                        parseFloat(text2Draw)
                    val states = rangeSeekBarState
                    if (compareFloat(
                            num,
                            states[0].value.toFloat()
                        ) != -1 && compareFloat(
                            num,
                            states[1].value.toFloat()
                        ) != 1 && getSeekBarMode() == SEEK_BAR_MODE_RANGE
                    ) {
                        paint.color = tickMarkInRangeTextColor
                    }
                    //按实际比例显示
                    x =
                        (progressLeft + progressWidth * (num - minProgress) / (maxProgress - minProgress)
                                - tickMarkTextRect.width() / 2f)
                }
                val y: Float = if (tickMarkLayoutGravity == Gravity.TOP) {
                    progressTop - tickMarkTextMargin.toFloat()
                } else {
                    progressBottom + tickMarkTextMargin + tickMarkTextRect.height().toFloat()
                }
                var degrees = 0
                val rotateX = x + tickMarkTextRect.width() / 2f
                val rotateY = y - tickMarkTextRect.height() / 2f
                if (tickMarkDirection == TEXT_DIRECTION_VERTICAL) {
                    if (orientation == DIRECTION_LEFT) {
                        degrees = 90
                    } else if (orientation == DIRECTION_RIGHT) {
                        degrees = -90
                    }
                }
                if (degrees != 0) {
                    canvas.rotate(degrees.toFloat(), rotateX, rotateY)
                }
                canvas.drawText(text2Draw, x, y, paint)
                if (degrees != 0) {
                    canvas.rotate(-degrees.toFloat(), rotateX, rotateY)
                }
            }
        }
    }

    override val tickMarkRawHeight: Int
        get() {
            if (maxTickMarkWidth > 0) return tickMarkTextMargin + maxTickMarkWidth
            if (tickMarkTextArray!!.isNotEmpty()) {
                val arrayLength: Int = tickMarkTextArray!!.size
                maxTickMarkWidth = measureText(
                    tickMarkTextArray!![0].toString(),
                    tickMarkTextSize.toFloat()
                ).width()
                for (i in 1 until arrayLength) {
                    val width = measureText(
                        tickMarkTextArray!![i].toString(), tickMarkTextSize.toFloat()
                    ).width()
                    if (maxTickMarkWidth < width) {
                        maxTickMarkWidth = width
                    }
                }
                return tickMarkTextMargin + maxTickMarkWidth
            }
            return 0
        }

    override var tickMarkTextSize: Int
        get() = super.tickMarkTextSize
        set(tickMarkTextSize) {
            super.tickMarkTextSize = tickMarkTextSize
            maxTickMarkWidth = 0
        }

    override var tickMarkTextArray
        get() = super.tickMarkTextArray
        set(tickMarkTextArray) {
            super.tickMarkTextArray = tickMarkTextArray
            maxTickMarkWidth = 0
        }

    override fun getEventX(event: MotionEvent): Float {
        return if (orientation == DIRECTION_LEFT) {
            height - event.y
        } else {
            event.y
        }
    }

    override fun getEventY(event: MotionEvent): Float {
        return if (orientation == DIRECTION_LEFT) {
            event.x
        } else {
            -event.x + width
        }
    }

    companion object {
        const val TEXT_DIRECTION_VERTICAL = 1
        const val DIRECTION_LEFT = 1
        const val DIRECTION_RIGHT = 2
    }

    init {
        initAttrs(attrs)
        initSeekBar(attrs)
    }
}
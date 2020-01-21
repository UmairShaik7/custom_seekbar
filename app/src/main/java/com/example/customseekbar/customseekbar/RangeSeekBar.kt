package com.example.customseekbar.customseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.customseekbar.R
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class RangeSeekBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    object Gravity {
        const val TOP = 0
        const val BOTTOM = 1
        const val CENTER = 2
    }

    private var seekBarMode = 0
    /**
     * [.TICK_MARK_GRAVITY_LEFT] is number tick mark, it will locate the position according to the value.
     * [.TICK_MARK_GRAVITY_RIGHT] is text tick mark, it will be equally positioned.
     *
     *
     */
    var tickMarkMode = 0

    //The spacing between the tick mark and the progress bar
    var tickMarkTextMargin = 0

    //tick mark text and prompt text size
    open var tickMarkTextSize = 0
    /**
     * the tick mark text gravity
     * [.TICK_MARK_GRAVITY_LEFT]
     * [.TICK_MARK_GRAVITY_RIGHT]
     * [.TICK_MARK_GRAVITY_CENTER]
     *
     */
    var tickMarkGravity = 0
    /**
     * the tick mark layout gravity
     * Gravity.TOP and Gravity.BOTTOM
     *
     */
    var tickMarkLayoutGravity = 0
    var tickMarkTextColor = 0
    var tickMarkInRangeTextColor = 0

    //The texts displayed on the scale
    open var tickMarkTextArray: Array<CharSequence>? = null
    //radius of progress bar
    private var progressRadius = 0f

    //the color of seekBar in progress
    private var progressColor = 0
    //the default color of the progress bar
    private var progressDefaultColor = 0
    //the drawable of seekBar in progress
    private var progressDrawableId = 0
    //the default Drawable of the progress bar
    private var progressDefaultDrawableId = 0
    //the progress height
    var progressHeight = 0
    // the progress width
    var progressWidth = 0
    //the range interval of RangeSeekBar
    private var minInterval = 0f
    /**
     * the RangeSeekBar gravity
     * Gravity.TOP and Gravity.BOTTOM
     */
    var gravity = 0
    //the color of step divs
    private var stepsColor = 0
    //the width of each step
    private var stepsWidth = 0f
    //the height of each step
    private var stepsHeight = 0f
    //the radius of step divs
    private var stepsRadius = 0f
    //steps is 0 will disable StepSeekBar
    private var steps = 0
    //the thumb will automatic bonding close to its value
    private var isStepsAutoBonding = false
    private var stepsDrawableId = 0
    //True values set by the user
    var minProgress = 0f
    var maxProgress = 0f
    //****************** the above is attr value  ******************//
    private var isEnable = true
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var reservePercent = 0f
    private var isScaleThumb = false
    private var paint = Paint()
    private var progressDefaultDstRect = RectF()
    private var progressDstRect = RectF()
    private var progressSrcRect = Rect()
    @JvmField
    var tickMarkTextRect = Rect()
    /**
     * if is single mode, please use it to get the SeekBar
     *
     * @return left seek bar
     */
    open var leftSeekBar: SeekBar? = null
    open var rightSeekBar: SeekBar? = null
    private var currTouchSB: SeekBar? = null
    private var progressBitmap: Bitmap? = null
    private var progressDefaultBitmap: Bitmap? = null
    private var stepsBitmaps: ArrayList<Bitmap> = ArrayList()
    var progressPaddingRight = 0

    private var callback: OnRangeChangedListener? = null
    private fun initProgressBitmap() {
        if (progressBitmap == null) {
            progressBitmap = Utils.drawableToBitmap(
                context,
                progressWidth,
                progressHeight,
                progressDrawableId
            )
        }
        if (progressDefaultBitmap == null) {
            progressDefaultBitmap = Utils.drawableToBitmap(
                context,
                progressWidth,
                progressHeight,
                progressDefaultDrawableId
            )
        }
    }

    private fun verifyStepsMode(): Boolean {
        return !(steps < 1 || stepsHeight <= 0 || stepsWidth <= 0)
    }

    private fun initStepsBitmap() {
        if (!verifyStepsMode() || stepsDrawableId == 0) return
        if (stepsBitmaps.isEmpty()) {
            val bitmap = Utils.drawableToBitmap(
                context,
                stepsWidth.toInt(),
                stepsHeight.toInt(),
                stepsDrawableId
            )
            for (i in 0..steps) {
                bitmap?.let { stepsBitmaps.add(it) }
            }
        }
    }

    private fun initSeekBar(attrs: AttributeSet?) {
        leftSeekBar = attrs?.let { SeekBar(this, it, false) }
    }

    private fun initAttrs(attrs: AttributeSet?) {
        try {
            val t = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar)
            seekBarMode = t.getInt(
                R.styleable.RangeSeekBar_rsb_mode,
                SEEK_BAR_MODE_RANGE
            )
            minProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_min, 0f)
            maxProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_max, 100f)
            minInterval = t.getFloat(R.styleable.RangeSeekBar_rsb_min_interval, 0f)
            gravity = t.getInt(
                R.styleable.RangeSeekBar_rsb_gravity,
                Gravity.TOP
            )
            progressColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_color, -0xb4269e)
            progressRadius =
                t.getDimension(R.styleable.RangeSeekBar_rsb_progress_radius, -1f)
            progressDefaultColor =
                t.getColor(R.styleable.RangeSeekBar_rsb_progress_default_color, -0x282829)
            progressDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable, 0)
            progressDefaultDrawableId =
                t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable_default, 0)
            progressHeight = t.getDimension(
                R.styleable.RangeSeekBar_rsb_progress_height,
                Utils.dp2px(context, 2f).toFloat()
            ).toInt()
            tickMarkMode = t.getInt(
                R.styleable.RangeSeekBar_rsb_tick_mark_mode,
                TRICK_MARK_MODE_NUMBER
            )
            tickMarkGravity = t.getInt(
                R.styleable.RangeSeekBar_rsb_tick_mark_gravity,
                TICK_MARK_GRAVITY_CENTER
            )
            tickMarkLayoutGravity = t.getInt(
                R.styleable.RangeSeekBar_rsb_tick_mark_layout_gravity,
                Gravity.TOP
            )
            tickMarkTextArray = t.getTextArray(R.styleable.RangeSeekBar_rsb_tick_mark_text_array)
            tickMarkTextMargin = t.getDimension(
                R.styleable.RangeSeekBar_rsb_tick_mark_text_margin,
                Utils.dp2px(context, 7f).toFloat()
            ).toInt()
            tickMarkTextSize = t.getDimension(
                R.styleable.RangeSeekBar_rsb_tick_mark_text_size,
                Utils.dp2px(context, 12f).toFloat()
            ).toInt()
            tickMarkTextColor =
                t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_text_color, progressDefaultColor)
            tickMarkInRangeTextColor =
                t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_text_color, progressColor)
            steps = t.getInt(R.styleable.RangeSeekBar_rsb_steps, 0)
            stepsColor = t.getColor(R.styleable.RangeSeekBar_rsb_step_color, -0x626263)
            stepsRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_step_radius, 0f)
            stepsWidth = t.getDimension(R.styleable.RangeSeekBar_rsb_step_width, 0f)
            stepsHeight = t.getDimension(R.styleable.RangeSeekBar_rsb_step_height, 0f)
            stepsDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_step_drawable, 0)
            isStepsAutoBonding = t.getBoolean(R.styleable.RangeSeekBar_rsb_step_auto_bonding, true)
            t.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * measure progress bar position
     */
    private fun onMeasureProgress(w: Int, h: Int) {
        val viewHeight =
            h - paddingBottom - paddingTop - PADDING_LEFT_SEEKBAR
        if (h <= 0) return
        when (gravity) {
            Gravity.TOP -> { //calculate the height of indicator and thumb exceeds the part of the progress
                var maxIndicatorHeight = 0f
                if (leftSeekBar!!.indicatorShowMode != SeekBar.INDICATOR_ALWAYS_HIDE
                    || rightSeekBar!!.indicatorShowMode != SeekBar.INDICATOR_ALWAYS_HIDE
                ) {
                    maxIndicatorHeight = max(
                        leftSeekBar!!.indicatorRawHeight,
                        rightSeekBar!!.indicatorRawHeight
                    ).toFloat()
                }
                var thumbHeight =
                    max(leftSeekBar!!.thumbScaleHeight, rightSeekBar!!.thumbScaleHeight)
                thumbHeight -= progressHeight / 2f
                //default height is indicator + thumb exceeds the part of the progress bar
                //if tickMark height is greater than (indicator + thumb exceeds the part of the progress)
                progressTop = (maxIndicatorHeight + (thumbHeight - progressHeight) / 2f).toInt()
                if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.TOP) {
                    progressTop = max(
                        tickMarkRawHeight.toFloat(),
                        maxIndicatorHeight + (thumbHeight - progressHeight) / 2f
                    ).toInt()
                }
                progressBottom = progressTop + progressHeight
            }
            Gravity.BOTTOM -> {
                progressBottom =
                    if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.BOTTOM) {
                        viewHeight - tickMarkRawHeight
                    } else {
                        (viewHeight - max(
                            leftSeekBar!!.thumbScaleHeight,
                            rightSeekBar!!.thumbScaleHeight
                        ) / 2f
                                + progressHeight / 2f).toInt()
                    }
                progressTop = progressBottom - progressHeight
            }
            else -> {
                progressTop = (viewHeight - progressHeight) / 2
                progressBottom = progressTop + progressHeight
            }
        }
        val maxThumbWidth =
            max(leftSeekBar!!.thumbScaleWidth, rightSeekBar!!.thumbScaleWidth).toInt()
        progressLeft = maxThumbWidth / 2 + paddingLeft
        progressRight = w - maxThumbWidth / 2 - paddingRight
        progressWidth = progressRight - progressLeft
        progressDefaultDstRect[progressLeft.toFloat(), progressTop + progressBottom / 2.toFloat(), progressRight.toFloat()] =
            progressBottom.toFloat()
        progressPaddingRight = w - progressRight
        //default value
        if (progressRadius <= 0) {
            progressRadius =
                ((progressBottom - progressTop) * 0.45f).toInt() + ((progressTop + progressBottom) / 4).toFloat()
        }
        initProgressBitmap()
    }

    //Android 7.0以后，优化了View的绘制，onMeasure和onSizeChanged调用顺序有所变化
//Android7.0以下：onMeasure--->onSizeChanged--->onMeasure
//Android7.0以上：onMeasure--->onSizeChanged
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        } else if (heightMode == MeasureSpec.AT_MOST && parent is ViewGroup
            && heightSize == ViewGroup.LayoutParams.MATCH_PARENT
        ) {
            heightSize = MeasureSpec.makeMeasureSpec(
                (parent as ViewGroup).measuredHeight,
                MeasureSpec.AT_MOST
            )
        } else {
            val heightNeeded: Int = if (gravity == Gravity.CENTER) {
                if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.BOTTOM) {
                    (2 * (rawHeight - tickMarkRawHeight)).toInt()
                } else {
                    (2 * (rawHeight - max(
                        leftSeekBar!!.thumbScaleHeight,
                        rightSeekBar!!.thumbScaleHeight
                    ) / 2)).toInt()
                }
            } else {
                rawHeight.toInt()
            }
            heightSize = MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightSize)
    }

    protected open val tickMarkRawHeight: Int
        get() = if (tickMarkTextArray != null && tickMarkTextArray!!.isNotEmpty()) {
            tickMarkTextMargin + Utils.measureText(
                tickMarkTextArray!![0].toString(),
                tickMarkTextSize.toFloat()
            ).height() + 3
        } else 0

    protected val rawHeight: Float
        get() {
            var rawHeight: Float
            if (seekBarMode == SEEK_BAR_MODE_SINGLE) {
                rawHeight = leftSeekBar!!.rawHeight
                if (tickMarkLayoutGravity == Gravity.BOTTOM && tickMarkTextArray != null) {
                    val h = max(
                        (leftSeekBar!!.thumbScaleHeight - progressHeight) / 2,
                        tickMarkRawHeight.toFloat()
                    )
                    rawHeight =
                        rawHeight - leftSeekBar!!.thumbScaleHeight / 2 + progressHeight / 2f + h
                }
            } else {
                rawHeight = max(leftSeekBar!!.rawHeight, rightSeekBar!!.rawHeight)
                if (tickMarkLayoutGravity == Gravity.BOTTOM && tickMarkTextArray != null) {
                    val thumbHeight =
                        max(leftSeekBar!!.thumbScaleHeight, rightSeekBar!!.thumbScaleHeight)
                    val h = max(
                        (thumbHeight - progressHeight) / 2,
                        tickMarkRawHeight.toFloat()
                    )
                    rawHeight = rawHeight - thumbHeight / 2 + progressHeight / 2f + h
                }
            }
            return rawHeight
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        onMeasureProgress(w, h)
        //set default value
        setRange(minProgress, maxProgress, minInterval)
        // initializes the positions of the two thumbs
        val lineCenterY = (progressBottom + progressTop) / 2
        leftSeekBar!!.onSizeChanged(progressLeft, lineCenterY)
        if (seekBarMode == SEEK_BAR_MODE_RANGE) {
            rightSeekBar!!.onSizeChanged(progressLeft, lineCenterY)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        onDrawTickMark(canvas, paint)
        onDrawProgressBar(canvas, paint)
        onDrawSeekBar(canvas)
    }

    //绘制刻度，并且根据当前位置是否在刻度范围内设置不同的颜色显示
// Draw the scales, and according to the current position is set within
// the scale range of different color display
    protected open fun onDrawTickMark(
        canvas: Canvas,
        paint: Paint
    ) {
        if (tickMarkTextArray != null) {
            val trickPartWidth = progressWidth / (tickMarkTextArray!!.size - 1)
            for (i in tickMarkTextArray!!.indices) {
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
                        Utils.parseFloat(text2Draw)
                    val states = rangeSeekBarState
                    if (Utils.compareFloat(
                            num,
                            states[0].value.toFloat()
                        ) != -1 && Utils.compareFloat(
                            num,
                            states[1].value.toFloat()
                        ) != 1 && seekBarMode == SEEK_BAR_MODE_RANGE
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
                canvas.drawText(text2Draw, x, y, paint)
            }
        }
    }


    // draw the progress bar
    private fun onDrawProgressBar(
        canvas: Canvas,
        paint: Paint
    ) { //draw default progress
        if (Utils.verifyBitmap(progressDefaultBitmap)) {
            canvas.drawBitmap(progressDefaultBitmap!!, null, progressDefaultDstRect, paint)
        } else {
            paint.color = progressDefaultColor
            canvas.drawRoundRect(progressDefaultDstRect, progressRadius, progressRadius, paint)
        }
        //draw progress
        if (seekBarMode == SEEK_BAR_MODE_RANGE) {
            progressDstRect.top = progressTop.toFloat()
            progressDstRect.left =
                leftSeekBar!!.left + leftSeekBar!!.thumbScaleWidth / 2f + progressWidth * leftSeekBar!!.currPercent
            progressDstRect.right =
                rightSeekBar!!.left + rightSeekBar!!.thumbScaleWidth / 2f + progressWidth * rightSeekBar!!.currPercent
            progressDstRect.bottom = progressBottom.toFloat()
        } else {
            progressDstRect.top = progressTop + progressBottom / 2.toFloat()
            progressDstRect.left = leftSeekBar!!.left + leftSeekBar!!.thumbScaleWidth / 2f
            progressDstRect.right =
                leftSeekBar!!.left + leftSeekBar!!.thumbScaleWidth / 2f + progressWidth * leftSeekBar!!.currPercent
            progressDstRect.bottom = progressBottom.toFloat()
        }
        if (Utils.verifyBitmap(progressBitmap)) {
            progressSrcRect.top = 0
            progressSrcRect.bottom = progressBitmap!!.height
            val bitmapWidth = progressBitmap!!.width
            if (seekBarMode == SEEK_BAR_MODE_RANGE) {
                progressSrcRect.left = (bitmapWidth * leftSeekBar!!.currPercent).toInt()
                progressSrcRect.right = (bitmapWidth * rightSeekBar!!.currPercent).toInt()
            } else {
                progressSrcRect.left = 0
                progressSrcRect.right = (bitmapWidth * leftSeekBar!!.currPercent).toInt()
            }
            canvas.drawBitmap(progressBitmap!!, progressSrcRect, progressDstRect, null)
        } else {
            paint.color = progressColor
            canvas.drawRoundRect(progressDstRect, progressRadius, progressRadius, paint)
        }
    }

    private fun onDrawSeekBar(canvas: Canvas?) { //draw left SeekBar
        if (leftSeekBar!!.indicatorShowMode == SeekBar.INDICATOR_ALWAYS_SHOW) {
            leftSeekBar!!.setShowIndicatorEnable(true)
        }
        canvas?.let { leftSeekBar!!.draw(it) }
        //draw right SeekBar
        if (seekBarMode == SEEK_BAR_MODE_RANGE) {
            if (rightSeekBar!!.indicatorShowMode == SeekBar.INDICATOR_ALWAYS_SHOW) {
                rightSeekBar!!.setShowIndicatorEnable(true)
            }
            canvas?.let { rightSeekBar!!.draw(it) }
        }
    }


    private fun initPaint() {
        paint.style = Paint.Style.FILL
        paint.color = progressDefaultColor
        paint.textSize = tickMarkTextSize.toFloat()
    }

    private fun changeThumbActivateState(hasActivate: Boolean) {
        if (hasActivate && currTouchSB != null) {
            val state = currTouchSB === leftSeekBar
            leftSeekBar!!.activate = state
            if (seekBarMode == SEEK_BAR_MODE_RANGE) rightSeekBar!!.activate = !state
        } else {
            leftSeekBar!!.activate = false
            if (seekBarMode == SEEK_BAR_MODE_RANGE) rightSeekBar!!.activate = false
        }
    }

    protected open fun getEventX(event: MotionEvent): Float {
        return event.x
    }

    protected open fun getEventY(event: MotionEvent): Float {
        return event.y
    }

    /**
     * scale the touch seekBar thumb
     */
    private fun scaleCurrentSeekBarThumb() {
        if (currTouchSB != null && currTouchSB!!.thumbScaleRatio > 1f && !isScaleThumb) {
            isScaleThumb = true
            currTouchSB!!.scaleThumb()
        }
    }

    /**
     * reset the touch seekBar thumb
     */
    private fun resetCurrentSeekBarThumb() {
        if (currTouchSB != null && currTouchSB!!.thumbScaleRatio > 1f && isScaleThumb) {
            isScaleThumb = false
            currTouchSB!!.resetThumb()
        }
    }

    //calculate currTouchSB percent by MotionEvent
    private fun calculateCurrentSeekBarPercent(touchDownX: Float): Float {
        if (currTouchSB == null) return 0F
        var percent = (touchDownX - progressLeft) * 1f / progressWidth
        if (touchDownX < progressLeft) {
            percent = 0f
        } else if (touchDownX > progressRight) {
            percent = 1f
        }
        //RangeMode minimum interval
        if (seekBarMode == SEEK_BAR_MODE_RANGE) {
            if (currTouchSB === leftSeekBar) {
                if (percent > rightSeekBar!!.currPercent - reservePercent) {
                    percent = rightSeekBar!!.currPercent - reservePercent
                }
            } else if (currTouchSB === rightSeekBar) {
                if (percent < leftSeekBar!!.currPercent + reservePercent) {
                    percent = leftSeekBar!!.currPercent + reservePercent
                }
            }
        }
        return percent
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnable) return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = getEventX(event)
                touchDownY = getEventY(event)
                if (seekBarMode == SEEK_BAR_MODE_RANGE) {
                    if (rightSeekBar!!.currPercent >= 1 && leftSeekBar!!.collide(
                            getEventX(event),
                            getEventY(event)
                        )
                    ) {
                        currTouchSB = leftSeekBar
                        scaleCurrentSeekBarThumb()
                    } else if (rightSeekBar!!.collide(getEventX(event), getEventY(event))) {
                        currTouchSB = rightSeekBar
                        scaleCurrentSeekBarThumb()
                    } else {
                        var performClick =
                            (touchDownX - progressLeft) * 1f / progressWidth
                        val distanceLeft =
                            abs(leftSeekBar!!.currPercent - performClick)
                        val distanceRight =
                            abs(rightSeekBar!!.currPercent - performClick)
                        currTouchSB = if (distanceLeft < distanceRight) {
                            leftSeekBar
                        } else {
                            rightSeekBar
                        }
                        performClick = calculateCurrentSeekBarPercent(touchDownX)
                        currTouchSB!!.slide(performClick)
                    }
                } else {
                    currTouchSB = leftSeekBar
                    scaleCurrentSeekBarThumb()
                }
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (callback != null) {
                    callback!!.onStartTrackingTouch(this, currTouchSB === leftSeekBar)
                }
                changeThumbActivateState(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val x = getEventX(event)
                if (seekBarMode == SEEK_BAR_MODE_RANGE && leftSeekBar!!.currPercent == rightSeekBar!!.currPercent) {
                    currTouchSB!!.materialRestore()
                    if (callback != null) {
                        callback!!.onStopTrackingTouch(this, currTouchSB === leftSeekBar)
                    }
                    if (x - touchDownX > 0) { //method to move right
                        if (currTouchSB !== rightSeekBar) {
                            currTouchSB!!.setShowIndicatorEnable(false)
                            resetCurrentSeekBarThumb()
                            currTouchSB = rightSeekBar
                        }
                    } else { //method to move left
                        if (currTouchSB !== leftSeekBar) {
                            currTouchSB!!.setShowIndicatorEnable(false)
                            resetCurrentSeekBarThumb()
                            currTouchSB = leftSeekBar
                        }
                    }
                    if (callback != null) {
                        callback!!.onStartTrackingTouch(this, currTouchSB === leftSeekBar)
                    }
                }
                scaleCurrentSeekBarThumb()
                currTouchSB!!.material =
                    if (currTouchSB!!.material >= 1) 1F else currTouchSB!!.material + 0.1f
                touchDownX = x
                currTouchSB!!.slide(calculateCurrentSeekBarPercent(touchDownX))
                currTouchSB!!.setShowIndicatorEnable(true)
                if (callback != null) {
                    val states = rangeSeekBarState
                    callback!!.onRangeChanged(
                        this,
                        states[0].value.toFloat(),
                        states[1].value.toFloat(),
                        true
                    )
                }
                invalidate()
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                changeThumbActivateState(true)
            }
            MotionEvent.ACTION_CANCEL -> {
                if (seekBarMode == SEEK_BAR_MODE_RANGE) {
                    rightSeekBar!!.setShowIndicatorEnable(false)
                }
                if (currTouchSB === leftSeekBar) {
                    resetCurrentSeekBarThumb()
                } else if (currTouchSB === rightSeekBar) {
                    resetCurrentSeekBarThumb()
                }
                leftSeekBar!!.setShowIndicatorEnable(false)
                if (callback != null) {
                    val states = rangeSeekBarState
                    callback!!.onRangeChanged(
                        this,
                        states[0].value.toFloat(),
                        states[1].value.toFloat(),
                        false
                    )
                }
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                changeThumbActivateState(false)
            }
            MotionEvent.ACTION_UP -> {
                if (verifyStepsMode() && isStepsAutoBonding) {
                    val percent = calculateCurrentSeekBarPercent(getEventX(event))
                    val stepPercent = 1.0f / steps
                    val stepSelected: Int = BigDecimal((percent / stepPercent).toDouble()).setScale(
                        0,
                        RoundingMode.HALF_UP
                    ).intValueExact()
                    currTouchSB!!.slide(stepSelected * stepPercent)
                }
                if (seekBarMode == SEEK_BAR_MODE_RANGE) {
                    rightSeekBar!!.setShowIndicatorEnable(false)
                }
                leftSeekBar!!.setShowIndicatorEnable(false)
                currTouchSB!!.materialRestore()
                resetCurrentSeekBarThumb()
                if (callback != null) {
                    val states = rangeSeekBarState
                    callback!!.onRangeChanged(
                        this,
                        states[0].value.toFloat(),
                        states[1].value.toFloat(),
                        false
                    )
                }
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (callback != null) {
                    callback!!.onStopTrackingTouch(this, currTouchSB === leftSeekBar)
                }
                changeThumbActivateState(false)
            }
        }
        return super.onTouchEvent(event)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss =
            SavedState(superState)
        ss.minValue = minProgress
        ss.maxValue = maxProgress
        ss.rangeInterval = minInterval
        val results = rangeSeekBarState
        ss.currSelectedMin = results[0].value.toFloat()
        ss.currSelectedMax = results[1].value.toFloat()
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        try {
            val ss =
                state as SavedState
            super.onRestoreInstanceState(ss.superState)
            val min = ss.minValue
            val max = ss.maxValue
            val rangeInterval = ss.rangeInterval
            setRange(min, max, rangeInterval)
            val currSelectedMin = ss.currSelectedMin
            val currSelectedMax = ss.currSelectedMax
            setProgress(currSelectedMin, currSelectedMax)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setProgress(leftValue: Float, rightValue: Float) {
        var lValue = leftValue
        var rValue = rightValue
        lValue = min(lValue, rValue)
        rValue = max(lValue, rValue)
        if (rValue - lValue < minInterval) {
            lValue = rValue - minInterval
        }
        require(lValue >= minProgress) { "setProgress() min < (preset min - offsetValue) . #min:$lValue #preset min:$rValue" }
        require(rValue <= maxProgress) { "setProgress() max > (preset max - offsetValue) . #max:$rValue #preset max:$rValue" }
        val range = maxProgress - minProgress
        leftSeekBar!!.currPercent = abs(lValue - minProgress) / range
        if (seekBarMode == SEEK_BAR_MODE_RANGE) {
            rightSeekBar!!.currPercent = abs(rValue - minProgress) / range
        }
        if (callback != null) {
            callback!!.onRangeChanged(this, lValue, rValue, false)
        }
        invalidate()
    }

    /**
     * 设置范围
     *
     * @param min         最小值
     * @param max         最大值
     * @param minInterval 最小间隔
     */
    private fun setRange(min: Float, max: Float, minInterval: Float) {
        require(max > min) { "setRange() max must be greater than min ! #max:$max #min:$min" }
        require(minInterval >= 0) { "setRange() interval must be greater than zero ! #minInterval:$minInterval" }
        require(minInterval < max - min) { "setRange() interval must be less than (max - min) ! #minInterval:" + minInterval + " #max - min:" + (max - min) }
        maxProgress = max
        minProgress = min
        this.minInterval = minInterval
        reservePercent = minInterval / (max - min)
        //set default value
        if (seekBarMode == SEEK_BAR_MODE_RANGE) {
            if (leftSeekBar!!.currPercent + reservePercent <= 1 && leftSeekBar!!.currPercent + reservePercent > rightSeekBar!!.currPercent) {
                rightSeekBar!!.currPercent = leftSeekBar!!.currPercent + reservePercent
            } else if (rightSeekBar!!.currPercent - reservePercent >= 0 && rightSeekBar!!.currPercent - reservePercent < leftSeekBar!!.currPercent) {
                leftSeekBar!!.currPercent = rightSeekBar!!.currPercent - reservePercent
            }
        }
        invalidate()
    }

    /**
     * @return the two seekBar state , see [SeekBarState]
     */
    val rangeSeekBarState: Array<SeekBarState>
        get() {
            val leftSeekBarState = SeekBarState()
            leftSeekBarState.value = leftSeekBar!!.progress.toInt()
            leftSeekBarState.indicatorText = leftSeekBarState.value.toString()
            if (Utils.compareFloat(
                    leftSeekBarState.value.toFloat(),
                    minProgress
                ) == 0
            ) {
                leftSeekBarState.isMin = true
            } else if (Utils.compareFloat(
                    leftSeekBarState.value.toFloat(),
                    maxProgress
                ) == 0
            ) {
                leftSeekBarState.isMax = true
            }
            val rightSeekBarState = SeekBarState()
            if (seekBarMode == SEEK_BAR_MODE_RANGE) {
                rightSeekBarState.value = rightSeekBar!!.progress.toInt()
                rightSeekBarState.indicatorText = rightSeekBarState.value.toString()
                if (Utils.compareFloat(
                        rightSeekBar!!.currPercent,
                        minProgress
                    ) == 0
                ) {
                    rightSeekBarState.isMin = true
                } else if (Utils.compareFloat(
                        rightSeekBar!!.currPercent,
                        maxProgress
                    ) == 0
                ) {
                    rightSeekBarState.isMax = true
                }
            }
            return arrayOf(leftSeekBarState, rightSeekBarState)
        }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        isEnable = enabled
    }


    fun getSeekBarMode(): Int {
        return seekBarMode
    }

    companion object {
        //normal seekBar mode
        const val SEEK_BAR_MODE_SINGLE = 1
        //RangeSeekBar
        const val SEEK_BAR_MODE_RANGE = 2
        //number according to the actual proportion of the number of arranged;
        const val TRICK_MARK_MODE_NUMBER = 0
        //other equally arranged
        const val TRICK_MARK_MODE_OTHER = 1
        const val TICK_MARK_GRAVITY_CENTER = 1
        const val TICK_MARK_GRAVITY_RIGHT = 2
        var progressTop = 0
        var progressBottom = 0
        var progressLeft = 0
        var progressRight = 0
        @JvmField
        var PADDING_RIGHT_SEEKBAR = 30
        protected var PADDING_LEFT_SEEKBAR = 160
    }

    init {
        initAttrs(attrs)
        initPaint()
        initSeekBar(attrs)
        initStepsBitmap()
    }
}
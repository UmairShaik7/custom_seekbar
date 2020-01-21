package com.example.customseekbar.customseekbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.customseekbar.R
import java.text.DecimalFormat

open class SeekBar(
    var rangeSeekBar: RangeSeekBar?,
    attrs: AttributeSet,
    private var isLeft: Boolean
) {

    /**
     * the indicator show mode
     * [.INDICATOR_SHOW_WHEN_TOUCH]
     * [.INDICATOR_ALWAYS_SHOW]
     * [.INDICATOR_ALWAYS_SHOW_AFTER_TOUCH]
     * [.INDICATOR_ALWAYS_SHOW]
     *
     */
    var indicatorShowMode = 0
    //进度提示背景的高度，宽度如果是0的话会自适应调整
//Progress prompted the background height, width,
    var indicatorHeight = 0
    var indicatorWidth = 0
    //进度提示背景与按钮之间的距离
//The progress indicates the distance between the background and the button
    var indicatorMargin = 0
    private var indicatorDrawableId = 0
    var indicatorArrowSize = 0
    var indicatorTextSize = 0
    var indicatorTextColor = 0
    var indicatorRadius = 0f
    var indicatorBackgroundColor = 0
    var indicatorPaddingLeft = 0
    var indicatorPaddingRight = 0
    var indicatorPaddingTop = 0
    var indicatorPaddingBottom = 0
    private var thumbDrawableId = 0
    private var thumbInactivatedDrawableId = 0
    private var thumbWidth = 0
    private var thumbHeight = 0
    /**
     * when you touch or move, the thumb will scale, default not scale
     *
     * @return default 1.0f
     */
    //when you touch or move, the thumb will scale, default not scale
    var thumbScaleRatio = 0f
    //****************** the above is attr value  ******************//
    var left = 0
    private var right = 0
    private var top = 0
    @JvmField
    var bottom = 0
    @JvmField
    var currPercent = 0f
    var material = 0f
    private var isShowIndicator = false
    private var thumbBitmap: Bitmap? = null
    private var thumbInactivatedBitmap: Bitmap? = null
    @JvmField
    var indicatorBitmap: Bitmap? = null
    private var anim: ValueAnimator? = null
    private var userText2Draw: String? = null
    var activate: Boolean = false
    /**
     * if visble is false, will clear the Canvas
     *
     */
    var isVisible = true
    private var indicatorTextStringFormat: String? = null
    @JvmField
    var indicatorArrowPath = Path()
    @JvmField
    var indicatorTextRect = Rect()
    @JvmField
    var indicatorRect = Rect()
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var indicatorTextDecimalFormat: DecimalFormat? = null
    @JvmField
    var scaleThumbWidth = 0
    @JvmField
    var scaleThumbHeight = 0

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet) {
        val t = context!!.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar) ?: return
        indicatorMargin = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_margin, 0f).toInt()
        indicatorDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_indicator_drawable, 0)
        indicatorShowMode = t.getInt(
            R.styleable.RangeSeekBar_rsb_indicator_show_mode,
            INDICATOR_ALWAYS_HIDE
        )
        indicatorHeight = t.getLayoutDimension(
            R.styleable.RangeSeekBar_rsb_indicator_height,
            WRAP_CONTENT
        )
        indicatorWidth = t.getLayoutDimension(
            R.styleable.RangeSeekBar_rsb_indicator_width,
            WRAP_CONTENT
        )
        indicatorTextSize = t.getDimension(
            R.styleable.RangeSeekBar_rsb_indicator_text_size,
            Utils.dp2px(context, 14f).toFloat()
        ).toInt()
        indicatorTextColor = t.getColor(
            R.styleable.RangeSeekBar_rsb_indicator_text_color,
            Color.WHITE
        )
        indicatorBackgroundColor = t.getColor(
            R.styleable.RangeSeekBar_rsb_indicator_background_color,
            ContextCompat.getColor(context!!, R.color.colorAccent)
        )
        indicatorPaddingLeft =
            t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_left, 0f).toInt()
        indicatorPaddingRight =
            t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_right, 0f).toInt()
        indicatorPaddingTop =
            t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_top, 0f).toInt()
        indicatorPaddingBottom =
            t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_bottom, 0f).toInt()
        indicatorArrowSize =
            t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_arrow_size, 0f).toInt()
        thumbDrawableId = t.getResourceId(
            R.styleable.RangeSeekBar_rsb_thumb_drawable,
            R.drawable.rsb_default_thumb
        )
        thumbInactivatedDrawableId =
            t.getResourceId(R.styleable.RangeSeekBar_rsb_thumb_inactivated_drawable, 0)
        thumbWidth = t.getDimension(
            R.styleable.RangeSeekBar_rsb_thumb_width,
            Utils.dp2px(context, 26f).toFloat()
        ).toInt()
        thumbHeight = t.getDimension(
            R.styleable.RangeSeekBar_rsb_thumb_height,
            Utils.dp2px(context, 26f).toFloat()
        ).toInt()
        thumbScaleRatio = t.getFloat(R.styleable.RangeSeekBar_rsb_thumb_scale_ratio, 1f)
        indicatorRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_radius, 0f)
        t.recycle()
    }

    private fun initVariables() {
        scaleThumbWidth = thumbWidth
        scaleThumbHeight = thumbHeight
        if (indicatorHeight == WRAP_CONTENT) {
            indicatorHeight = Utils.measureText(
                "2",
                indicatorTextSize.toFloat()
            ).height() + indicatorPaddingTop + indicatorPaddingBottom
        }
        if (indicatorArrowSize <= 0) {
            indicatorArrowSize = (thumbWidth / 4)
        }
    }

    val context: Context?
        get() = rangeSeekBar!!.context

    private val resources: Resources?
        get() = if (context != null) context!!.resources else null

    /**
     * 初始化进度提示的背景
     */
    private fun initBitmap() {
        setIndicatorDrawableId(indicatorDrawableId)
        setThumbDrawableId(thumbDrawableId, thumbWidth, thumbHeight)
        setThumbInactivatedDrawableId(thumbInactivatedDrawableId, thumbWidth, thumbHeight)
    }

    /**
     * 计算每个按钮的位置和尺寸
     * Calculates the position and size of each button
     *
     * @param x position x
     * @param y position y
     */
    fun onSizeChanged(x: Int, y: Int) {
        initVariables()
        initBitmap()
        left = (x - thumbScaleWidth / 2).toInt()
        right = (x + thumbScaleWidth / 2).toInt()
        top = y - thumbHeight / 2
        bottom = y + thumbHeight / 2
    }

    fun scaleThumb() {
        scaleThumbWidth = thumbScaleWidth.toInt()
        scaleThumbHeight = thumbScaleHeight.toInt()
        val y = RangeSeekBar.progressBottom
        top = y - scaleThumbHeight / 2
        bottom = y + scaleThumbHeight / 2
        setThumbDrawableId(thumbDrawableId, scaleThumbWidth, scaleThumbHeight)
    }

    fun resetThumb() {
        scaleThumbWidth = thumbWidth
        scaleThumbHeight = thumbHeight
        val y = RangeSeekBar.progressBottom
        top = y - scaleThumbHeight / 2
        bottom = y + scaleThumbHeight / 2
        setThumbDrawableId(thumbDrawableId, scaleThumbWidth, scaleThumbHeight)
    }

    val rawHeight: Float
        get() = indicatorHeight + indicatorArrowSize + indicatorMargin + thumbScaleHeight

    /**
     * 绘制按钮和提示背景和文字
     * Draw buttons and tips for background and text
     *
     * @param canvas Canvas
     */
    fun draw(canvas: Canvas) {
        if (!isVisible) {
            return
        }
        val offset = (rangeSeekBar!!.progressWidth * currPercent).toInt()
        canvas.save()
        canvas.translate(offset.toFloat(), RangeSeekBar.progressTop * .55f / 2)
        // translate canvas, then don't care left
        canvas.translate(left.toFloat(), 0f)
        if (isShowIndicator) {
            onDrawIndicator(canvas, paint, formatCurrentIndicatorText(userText2Draw))
        }
        onDrawThumb(canvas)
        canvas.restore()
    }

    /**
     * 绘制按钮
     * 如果没有图片资源，则绘制默认按钮
     *
     *
     * draw the thumb button
     * If there is no image resource, draw the default button
     *
     * @param canvas canvas
     */
    private fun onDrawThumb(canvas: Canvas) {
        if (thumbInactivatedBitmap != null && !activate) {
            canvas.drawBitmap(
                thumbInactivatedBitmap!!,
                0f,
                RangeSeekBar.progressTop + (rangeSeekBar!!.progressHeight - scaleThumbHeight) / 2f,
                null
            )
        } else if (thumbBitmap != null) {
            canvas.drawBitmap(
                thumbBitmap!!,
                0f,
                RangeSeekBar.progressTop + (rangeSeekBar!!.progressHeight - scaleThumbHeight) / 2f,
                null
            )
        }
    }

    /**
     * 格式化提示文字
     * format the indicator text
     *
     * @param text2Draw
     * @return
     */
    private fun formatCurrentIndicatorText(text2Draw: String?): String? {
        var str2Draw = text2Draw
        val states = rangeSeekBar!!.rangeSeekBarState
        if (TextUtils.isEmpty(str2Draw)) {
            str2Draw = if (isLeft) {
                if (indicatorTextDecimalFormat != null) {
                    indicatorTextDecimalFormat!!.format(states[0].value.toLong())
                } else {
                    states[0].indicatorText
                }
            } else {
                if (indicatorTextDecimalFormat != null) {
                    indicatorTextDecimalFormat!!.format(states[1].value.toLong())
                } else {
                    states[1].indicatorText
                }
            }
        }
        if (indicatorTextStringFormat != null) {
            str2Draw = String.format(indicatorTextStringFormat!!, str2Draw)
        }
        return str2Draw
    }

    /**
     * This method will draw the indicator background dynamically according to the text.
     * you can use to set padding
     *
     * @param canvas    Canvas
     * @param text2Draw Indicator text
     */
    protected open fun onDrawIndicator(
        canvas: Canvas,
        paint: Paint,
        text2Draw: String?
    ) {
        if (text2Draw == null) return
        paint.textSize = indicatorTextSize.toFloat()
        paint.style = Paint.Style.FILL
        paint.color = indicatorBackgroundColor
        paint.getTextBounds(text2Draw, 0, text2Draw.length, indicatorTextRect)
        var realIndicatorWidth =
            indicatorTextRect.width() + indicatorPaddingLeft + indicatorPaddingRight
        if (indicatorWidth > realIndicatorWidth) {
            realIndicatorWidth = indicatorWidth
        }
        var realIndicatorHeight =
            indicatorTextRect.height() + indicatorPaddingTop + indicatorPaddingBottom
        if (indicatorHeight > realIndicatorHeight) {
            realIndicatorHeight = indicatorHeight
        }
        indicatorRect.left = (scaleThumbWidth / 2f - realIndicatorWidth / 2f).toInt()
        indicatorRect.top = bottom - realIndicatorHeight - scaleThumbHeight - indicatorMargin
        indicatorRect.right = indicatorRect.left + realIndicatorWidth
        indicatorRect.bottom = indicatorRect.top + realIndicatorHeight
        //draw default indicator arrow
        if (indicatorBitmap == null) { //arrow three point
//  b   c
//    a
            val ax = scaleThumbWidth / 2
            val ay = indicatorRect.bottom
            val bx = ax - indicatorArrowSize
            val by = ay - indicatorArrowSize
            val cx = ax + indicatorArrowSize
            indicatorArrowPath.reset()
            indicatorArrowPath.moveTo(ax.toFloat(), ay.toFloat())
            indicatorArrowPath.lineTo(bx.toFloat(), by.toFloat())
            indicatorArrowPath.lineTo(cx.toFloat(), by.toFloat())
            indicatorArrowPath.close()
            canvas.drawPath(indicatorArrowPath, paint)
            indicatorRect.bottom -= indicatorArrowSize
            indicatorRect.top -= indicatorArrowSize
        }
        //indicator background edge processing
        val defaultPaddingOffset =
            Utils.dp2px(context, 1f)
        val leftOffset =
            indicatorRect.width() / 2 - (rangeSeekBar!!.progressWidth * currPercent).toInt() - RangeSeekBar.progressLeft + defaultPaddingOffset
        val rightOffset =
            indicatorRect.width() / 2 - (rangeSeekBar!!.progressWidth * (1 - currPercent)).toInt() - rangeSeekBar!!.progressPaddingRight + defaultPaddingOffset
        if (leftOffset > 0) {
            indicatorRect.left += leftOffset
            indicatorRect.right += leftOffset
        } else if (rightOffset > 0) {
            indicatorRect.left -= rightOffset
            indicatorRect.right -= rightOffset
        }
        //draw indicator background
        when {
            indicatorBitmap != null -> {
                Utils.drawBitmap(
                    canvas,
                    paint,
                    indicatorBitmap!!,
                    indicatorRect
                )
            }
            indicatorRadius > 0f -> {
                canvas.drawRoundRect(RectF(indicatorRect), indicatorRadius, indicatorRadius, paint)
            }
            else -> {
                canvas.drawRect(indicatorRect, paint)
            }
        }
        //draw indicator content text
        val tx: Int
        val ty: Int
        tx = when {
            indicatorPaddingLeft > 0 -> {
                indicatorRect.left + indicatorPaddingLeft
            }
            indicatorPaddingRight > 0 -> {
                indicatorRect.right - indicatorPaddingRight - indicatorTextRect.width()
            }
            else -> {
                indicatorRect.left + (realIndicatorWidth - indicatorTextRect.width()) / 2
            }
        }
        ty = when {
            indicatorPaddingTop > 0 -> {
                indicatorRect.top + indicatorTextRect.height() + indicatorPaddingTop
            }
            indicatorPaddingBottom > 0 -> {
                indicatorRect.bottom - indicatorTextRect.height() - indicatorPaddingBottom
            }
            else -> {
                indicatorRect.bottom - (realIndicatorHeight - indicatorTextRect.height()) / 2 + 1
            }
        }
        //draw indicator text
        paint.color = indicatorTextColor
        canvas.drawText(text2Draw, tx.toFloat(), ty.toFloat(), paint)
    }

    /**
     * 拖动检测
     *
     * @return is collide
     */
    fun collide(x: Float, y: Float): Boolean {
        val offset = (rangeSeekBar!!.progressWidth * currPercent).toInt()
        return x > left + offset && x < right + offset && y > top && y < bottom
    }

    fun slide(percent: Float) {
        var percent1 = percent
        if (percent1 < 0) percent1 = 0f else if (percent1 > 1) percent1 = 1f
        currPercent = percent1
    }

    fun setShowIndicatorEnable(isEnable: Boolean) {
        when (indicatorShowMode) {
            INDICATOR_SHOW_WHEN_TOUCH -> isShowIndicator =
                isEnable
            INDICATOR_ALWAYS_SHOW, INDICATOR_ALWAYS_SHOW_AFTER_TOUCH -> isShowIndicator =
                true
            INDICATOR_ALWAYS_HIDE -> isShowIndicator =
                false
        }
    }

    fun materialRestore() {
        if (anim != null) anim!!.cancel()
        anim = ValueAnimator.ofFloat(material, 0f)
        anim?.addUpdateListener { animation ->
            material = animation.animatedValue as Float
            if (rangeSeekBar != null) rangeSeekBar!!.invalidate()
        }
        anim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                material = 0f
                if (rangeSeekBar != null) rangeSeekBar!!.invalidate()
            }
        })
        anim?.start()
    }

    private fun setIndicatorDrawableId(@DrawableRes indicatorDrawableId: Int) {
        if (indicatorDrawableId != 0) {
            this.indicatorDrawableId = indicatorDrawableId
            indicatorBitmap = BitmapFactory.decodeResource(resources, indicatorDrawableId)
        }
    }

    /**
     * include indicator text Height、padding、margin
     *
     * @return The actual occupation height of indicator
     */
    val indicatorRawHeight: Int
        get() = if (indicatorHeight > 0) {
            if (indicatorBitmap != null) {
                indicatorHeight + indicatorMargin
            } else {
                indicatorHeight + indicatorArrowSize + indicatorMargin
            }
        } else {
            if (indicatorBitmap != null) {
                Utils.measureText(
                    "8",
                    indicatorTextSize.toFloat()
                ).height() + indicatorPaddingTop + indicatorPaddingBottom + indicatorMargin
            } else {
                Utils.measureText(
                    "8",
                    indicatorTextSize.toFloat()
                ).height() + indicatorPaddingTop + indicatorPaddingBottom + indicatorMargin + indicatorArrowSize
            }
        }
    @Suppress("DEPRECATION")
    private fun setThumbInactivatedDrawableId(
        @DrawableRes thumbInactivatedDrawableId: Int, width: Int,
        height: Int
    ) {
        if (thumbInactivatedDrawableId != 0 && resources != null) {
            this.thumbInactivatedDrawableId = thumbInactivatedDrawableId
            thumbInactivatedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Utils.drawableToBitmap(
                    width,
                    height,
                    resources!!.getDrawable(thumbInactivatedDrawableId, null)
                )
            } else {
                Utils.drawableToBitmap(width, height, resources!!.getDrawable(thumbInactivatedDrawableId))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setThumbDrawableId(
        @DrawableRes thumbDrawableId: Int, width: Int,
        height: Int
    ) {
        if (thumbDrawableId != 0 && resources != null && width > 0 && height > 0) {
            this.thumbDrawableId = thumbDrawableId
            thumbBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Utils.drawableToBitmap(
                    width,
                    height,
                    resources!!.getDrawable(thumbDrawableId, null)
                )
            } else {
                Utils.drawableToBitmap(
                    width,
                    height,
                    resources!!.getDrawable(thumbDrawableId)
                )
            }
        }
    }

    @Suppress("UNUSED","DEPRECATION")
    fun setThumbDrawableId(@DrawableRes thumbDrawableId: Int) {
        require(!(thumbWidth <= 0 || thumbHeight <= 0)) { "please set thumbWidth and thumbHeight first!" }
        if (thumbDrawableId != 0 && resources != null) {
            this.thumbDrawableId = thumbDrawableId
            thumbBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Utils.drawableToBitmap(
                    thumbWidth,
                    thumbHeight,
                    resources!!.getDrawable(thumbDrawableId, null)
                )
            } else {
                Utils.drawableToBitmap(
                    thumbWidth,
                    thumbHeight,
                    resources!!.getDrawable(thumbDrawableId)
                )
            }
        }
    }

    val thumbScaleHeight: Float
        get() = thumbHeight * thumbScaleRatio

    val thumbScaleWidth: Float
        get() = thumbWidth * thumbScaleRatio

    val progress: Float
        get() {
            val range = rangeSeekBar!!.maxProgress - rangeSeekBar!!.minProgress
            return rangeSeekBar!!.minProgress + range * currPercent
        }

    companion object {
        //the indicator show mode
        const val INDICATOR_SHOW_WHEN_TOUCH = 0
        const val INDICATOR_ALWAYS_HIDE = 1
        const val INDICATOR_ALWAYS_SHOW_AFTER_TOUCH = 2
        const val INDICATOR_ALWAYS_SHOW = 3
        const val WRAP_CONTENT = -1
    }

    init {
        initAttrs(attrs)
        initBitmap()
        initVariables()
    }
}
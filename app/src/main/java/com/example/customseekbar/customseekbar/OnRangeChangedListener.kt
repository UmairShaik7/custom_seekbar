package com.example.customseekbar.customseekbar

interface OnRangeChangedListener {
    fun onRangeChanged(
        view: RangeSeekBar?,
        leftValue: Float,
        rightValue: Float,
        isFromUser: Boolean
    )

    fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean)
    fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean)
}
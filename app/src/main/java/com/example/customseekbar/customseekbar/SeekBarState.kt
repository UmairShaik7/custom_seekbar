package com.example.customseekbar.customseekbar

class SeekBarState {
    var indicatorText: String? = null
    @JvmField
    var value //now progress value
            = 0
    var isMin = false
    var isMax = false
    override fun toString(): String {
        return "indicatorText: $indicatorText ,isMin: $isMin ,isMax: $isMax"
    }
}
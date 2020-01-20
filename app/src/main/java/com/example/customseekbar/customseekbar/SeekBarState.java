package com.example.customseekbar.customseekbar;

public class SeekBarState {
    public String indicatorText;
    public int value; //now progress value
    public boolean isMin;
    public boolean isMax;

    @Override
    public String toString() {
        return "indicatorText: " + indicatorText + " ,isMin: " + isMin + " ,isMax: " + isMax;
    }
}

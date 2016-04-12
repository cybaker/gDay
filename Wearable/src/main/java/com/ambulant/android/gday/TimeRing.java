package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

/**
 * Time ring that starts at 00 hrs and sweeps through to the current time.
 */
public class TimeRing extends Ring {
    ArcSegment gPercent;

    public TimeRing(float centerX, float centerY, float radius, float thickness) {
        super(centerX, centerY, radius, thickness);

        // A wide circle
        gPercent = new ArcSegment(-90, 120, radius, thickness, Color.BLACK, Color.BLACK, "", 12, NORMAL_TYPEFACE, Color.WHITE);
        add(gPercent);
    }

    /**
     * Set the percentage from 0 to 360 degrees
     * @param hoursSinceMidnight number of hours since midnight
     */
    public void setTime(float hoursSinceMidnight) {
        if(hoursSinceMidnight < 0 || hoursSinceMidnight > 24) {
            Log.e("TimeRing", "setTime error. Hours since midnight out of range = "+hoursSinceMidnight);
            return;
        }

        float degreesSinceMidnight = hoursSinceMidnight * 360 / 24;

        if(gPercent != null) {
            gPercent.setSweepDegrees(degreesSinceMidnight);
            gPercent.setStartDegrees(90);
        }
    }

    public void setCenter(int x, int y) {
        gCenterX = x;
        gCenterY = y;
    }

    public void setFillColor(int color) {
        gPercent.setStrokePaint(gPercent.createFillPaint(color, color));
    }

    public void setStrokeColor(int color) {
        gPercent.setStrokePaint(gPercent.createStrokePaint(color));
    }

    @Override
    protected void onDraw(Canvas canvas, boolean ambientMode) {
        super.onDraw(canvas, false);
    }
}

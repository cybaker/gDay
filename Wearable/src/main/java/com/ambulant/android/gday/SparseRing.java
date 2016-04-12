package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * Sparse ring of segments
 */
public class SparseRing extends Ring {

    public SparseRing(float centerX, float centerY, float radius, float thickness) {
        super(centerX, centerY, radius, thickness);

        boolean is24hr = false;
        float oneHour;
        float offset;
        if(is24hr) {
            oneHour = 360/24f;
            offset = 90;
        } else {
            oneHour = 360/12f;
            offset = -90;
        }

        // Add a number of segments with some text
        add(new ArcSegment(offset + 7*oneHour, 1*oneHour, gRadius, gThickness, Color.DKGRAY, Color.DKGRAY, "1", 12, NORMAL_TYPEFACE, Color.WHITE));
        add(new ArcSegment(offset + 8*oneHour, 1.5f*oneHour, gRadius, gThickness, Color.LTGRAY, Color.DKGRAY, "2", 12, NORMAL_TYPEFACE, Color.WHITE));
        add(new ArcSegment(offset + 10*oneHour, 1*oneHour, gRadius, gThickness, Color.GRAY, Color.DKGRAY, "3", 12, NORMAL_TYPEFACE, Color.WHITE));
        add(new ArcSegment(offset + 12*oneHour, oneHour/2, gRadius, gThickness, Color.GRAY, Color.DKGRAY, "4", 12, NORMAL_TYPEFACE, Color.WHITE));
        add(new ArcSegment(offset + 14*oneHour, oneHour/2, gRadius, gThickness, Color.LTGRAY, Color.DKGRAY, "5", 12, NORMAL_TYPEFACE, Color.WHITE));
    }

    @Override
    protected void onDraw(Canvas canvas, boolean ambientMode) {
        super.onDraw(canvas, ambientMode);
    }
}

package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * A collection of Arc Segments centered at a location with radius and thickness
 */
public class Ticks {

    protected float gCenterX, gCenterY, gInnerRadius, gOuterRadius;
    protected int gHoursPerCircle;
    protected Paint gTickPaint;

    public Ticks(float centerX, float centerY, float innerRadius, float outerRadius, int hoursPerCircle, Paint tickPaint) {
        gCenterX = centerX;
        gCenterY = centerY;
        gInnerRadius = innerRadius;
        gOuterRadius = outerRadius;
        gHoursPerCircle = hoursPerCircle;
        gTickPaint = tickPaint;
    }

    protected void onDraw(Canvas canvas, boolean ambientMode) {
        if(!ambientMode) {
            // Draw the ticks
            for (int tickIndex = 0; tickIndex < gHoursPerCircle; tickIndex++) {
                float tickRot = (float) (tickIndex * Math.PI * 2 / gHoursPerCircle);
                float innerX = (float) Math.sin(tickRot) * gInnerRadius;
                float innerY = (float) -Math.cos(tickRot) * gInnerRadius;
                float outerX = (float) Math.sin(tickRot) * gOuterRadius;
                float outerY = (float) -Math.cos(tickRot) * gOuterRadius;
                canvas.drawLine(gCenterX + innerX, gCenterY + innerY,
                        gCenterX + outerX, gCenterY + outerY, gTickPaint);
            }
        }
    }
}

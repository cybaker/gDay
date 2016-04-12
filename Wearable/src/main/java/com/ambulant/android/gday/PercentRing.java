package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Percent ring showing 0 to 100 percent from noon position
 */
public class PercentRing extends Ring {
    ArcSegment mPercentArc;
    String gText;
    Paint gTextPaint;
    Color mColor;

    public PercentRing(float centerX, float centerY, float radius, float thickness, String text) {
        super(centerX, centerY, radius, thickness);

        gText = text;
        if(gText != null) {
            gTextPaint = createTextPaint(Color.WHITE);
        }
        mPercentArc = new ArcSegment(-90, 120, radius, thickness, 0, Color.RED, "", 12, NORMAL_TYPEFACE, Color.WHITE);
        add(mPercentArc);
    }

    /**
     * Set the percentage from 0 to 100f
     * @param percent
     */
    public void setPercent(float percent) {
        percent = percent < 0.0f ? 0.0f : percent;
        percent = percent > 100.0f ? 100.0f : percent;

        float degrees = percent * 360/100;

        if(mPercentArc != null) {
            mPercentArc.setSweepDegrees(degrees);
        }
    }

    public void setCenter(int x, int y) {
        gCenterX = x;
        gCenterY = y;
    }

    public void setColor(int color) {
        Paint paint = mPercentArc.createFillPaint(color, color);
        mPercentArc.setFillPaint(paint);
        mPercentArc.setStrokePaint(paint);
    }

    private Paint createTextPaint(int defaultInteractiveColor) {
        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setColor(defaultInteractiveColor);
        paint.setAntiAlias(true);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas, boolean ambientMode) {
        super.onDraw(canvas, ambientMode);

        if(gText != null && gTextPaint != null) {
            canvas.drawText(gText, gCenterX - gRadius/4, gCenterY + gRadius/4, gTextPaint);
        }
    }
}

package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Arc as part of a Ring
 */
public class ArcSegment {

    private float gStartDeg, gSweepDeg, gRadius, gThickness;
    private String gText;
    private Paint gFillPaint, gStrokePaint, gTextPaint;

    /**
     * An arc segment with thickness, radius, color, and text
     * @param startDeg starting degrees from "3 o'clock", or East
     * @param sweepDeg sweep degrees of the arc from start
     * @param radius    radius of the arc defines the curvature
     * @param thickness thickness of the segment
     * @param fillColor fill color when ambient is off. Will fill with black otherwise
     * @param text  optional text to show
     * @param textSize size of optional text
     * @param textTypeface typeface of optional text
     * @param textColor color of optional text
     */
    public ArcSegment(float startDeg, float sweepDeg, float radius, float thickness, int fillColor, int strokeColor, String text, float textSize, Typeface textTypeface, int textColor) {
        gStartDeg = startDeg;
        gSweepDeg = sweepDeg;
        gRadius = radius;
        gThickness = thickness;
        gText = text;

        gTextPaint = createTextPaint(textColor, textTypeface);
        gTextPaint.setTextSize(textSize);

        gFillPaint = createFillPaint(fillColor, strokeColor);
        if(strokeColor != 0) {
            gStrokePaint = createStrokePaint(strokeColor);
        }
    }

    private static final float CIRCLE_LIMIT = 359.9999f;
    /**
     * See http://stackoverflow.com/questions/3874424/android-looking-for-a-drawarc-method-with-inner-outer-radius
     *
     * Draws a thick arc between the defined angles, see {@link Canvas#drawArc} for more.
     * This method is equivalent to
     * <pre><code>
     * float rMid = (radius + rOut) / 2;
     * paint.setStyle(Style.STROKE); // there's nothing to fill
     * paint.setStrokeWidth(rOut - radius); // thickness
     * canvas.drawArc(new RectF(cx - rMid, cy - rMid, cx + rMid, cy + rMid), startAngle, sweepAngle, false, paint);
     * </code></pre>
     * but supports different fill and stroke paints.
     *
     * @param canvas
     * @param centerX horizontal middle point of the oval
     * @param centerY vertical middle point of the oval
     * @see Canvas#drawArc
     */
    public void onDraw(Canvas canvas, float centerX, float centerY, boolean ambientMode) {
        if (gSweepDeg > CIRCLE_LIMIT) {
            gSweepDeg = CIRCLE_LIMIT;
        }
        if (gSweepDeg < -CIRCLE_LIMIT) {
            gSweepDeg = -CIRCLE_LIMIT;
        }

        //TODO move these to constructor
        RectF innerRect = new RectF(centerX - gRadius + gThickness/2, centerY - gRadius + gThickness/2, centerX + gRadius - gThickness/2, centerY + gRadius - gThickness/2);
        RectF outerRect = new RectF(centerX - gRadius - gThickness/2, centerY - gRadius - gThickness/2, centerX + gRadius + gThickness/2, centerY + gRadius + gThickness/2);

        if(ambientMode) {
            if(gStrokePaint != null) gStrokePaint.setAlpha(0x80);
            if(gFillPaint != null) gFillPaint.setAlpha(0x40);
        } else {
            if(gStrokePaint != null) gStrokePaint.setAlpha(0xFF);
            if(gFillPaint != null) gFillPaint.setAlpha(0xFF);
        }

        if (gStrokePaint != null) {
            Path strokePath = new Path(); //TODO move
            strokePath.addArc(innerRect, gStartDeg, gSweepDeg);
            strokePath.arcTo(outerRect, gStartDeg + gSweepDeg, -gSweepDeg);
            strokePath.close();
            canvas.drawPath(strokePath, gStrokePaint);
        }

        RectF midRect = new RectF(centerX - gRadius, centerY - gRadius, centerX + gRadius, centerY + gRadius);

        if(gFillPaint != null) {
            Path fillPath = new Path(); // TODO move
            fillPath.addArc(midRect, gStartDeg, gSweepDeg);
            canvas.drawPath(fillPath, gFillPaint);
        }

        // Draw text if any along the arc in all cases
        if(gText != null && gTextPaint != null) {
            gTextPaint.setTextAlign(Paint.Align.CENTER);
            Path midway = new Path();
            RectF segment = new RectF(centerX - gRadius, centerY - gRadius, centerX + gRadius, centerY + gRadius);
            midway.addArc(segment, gStartDeg, gSweepDeg);
            canvas.drawTextOnPath(gText, midway, 0, 4, gTextPaint);
        }
    }

    public void setText(String text) {
        gText = text;
    }

    public void setFillPaint(Paint fill) {
        gFillPaint = fill;
    }

    public void setStrokePaint(Paint fill) {
        gStrokePaint = fill;
    }

    public void setSweepDegrees(float degrees) {
        gSweepDeg = degrees;
    }

    public void setStartDegrees(float degrees) {
        gStartDeg = degrees;
    }

    private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
        Paint paint = new Paint();
        paint.setColor(defaultInteractiveColor);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        return paint;
    }

    public Paint createFillPaint(int color, int shadowColor) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setFilterBitmap(true);
        paint.setStrokeWidth(gThickness);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShadowLayer(5, 0, 0, shadowColor);
        return paint;
    }

    public Paint createStrokePaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }
}

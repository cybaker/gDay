package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of Arc Segments centered at a location with radius and thickness
 */
public class Ring {
    protected static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    protected static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    // The arc segments to get drawn
    protected List<ArcSegment> gArcs = new ArrayList<>();

    protected float gCenterX, gCenterY, gRadius, gThickness;

    public Ring(float centerX, float centerY, float radius, float thickness) {
        gCenterX = centerX;
        gCenterY = centerY;
        gRadius = radius;
        gThickness = thickness;
    }

    protected void add(ArcSegment segment) {
        gArcs.add(segment);
    }

    protected void onDraw(Canvas canvas, boolean ambientMode) {
        for(ArcSegment segment : gArcs) {
            segment.onDraw(canvas, gCenterX, gCenterY, ambientMode);
        }
    }
}

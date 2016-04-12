package com.ambulant.android.gday;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date ring showing day and date
 */
public class DateRing extends Ring {
    private Calendar mCalendar;
    private Date mDate;
    SimpleDateFormat mDayOfWeekFormat;
    java.text.DateFormat mDateFormat;
    Context gContext;

    ArcSegment day;

    public DateRing(Context context, float centerX, float centerY, float radius, float thickness) {
        super(centerX, centerY, radius, thickness);

        gContext = context;
        mCalendar = Calendar.getInstance();
        mDate = new Date();

        // A segment with the day of week and date
        day = new ArcSegment(-125, 70, radius, thickness, Color.TRANSPARENT, Color.TRANSPARENT, "Sparta", 20, NORMAL_TYPEFACE, Color.WHITE);
        add(day);
    }

    @Override
    protected void onDraw(Canvas canvas, boolean ambientMode) {
        if(!ambientMode) {
            mCalendar.setTimeZone(TimeZone.getDefault());

            mDayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            mDayOfWeekFormat.setCalendar(mCalendar);
            mDateFormat = DateFormat.getDateFormat(gContext);
            mDateFormat.setCalendar(mCalendar);

            String dayOfWeek = mDayOfWeekFormat.format(mDate);
            String dateFormatted = mDateFormat.format(mDate);

            day.setText(dayOfWeek + " " + dateFormatted);

            super.onDraw(canvas, ambientMode);
        }
    }
}

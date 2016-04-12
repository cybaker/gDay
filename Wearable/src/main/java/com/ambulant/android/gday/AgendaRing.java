package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;

import com.ambulant.android.gday.models.CalendarEvent;
import com.example.android.wearable.watchface.BuildConfig;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Sparse ring of segments for an agenda from the user's calendar
 */
public class AgendaRing extends Ring {
    private int gHoursPerCircle;
    private float gDegreesPerHour;
    private float gDegreeOffsetByHours = 90; // degrees of rotation for the beginning: 90 for 24 hr clock, -90 for 12 hr clock

    /**
     * An Agenda sparse ring showing busy times around the clock
     * @param centerX the x position of the circle's center
     * @param centerY the y position of the circle's center
     * @param radius    the radius of the circle arcs are placed on from the center
     * @param thickness the thickness of the circle spreading closer and farther from the radius
     * @param hoursPerCircle the number of time divisions on the circle. 24 for 24 hours of time.
     */
    public AgendaRing(float centerX, float centerY, float radius, float thickness, int hoursPerCircle) {
        super(centerX, centerY, radius, thickness);

        gHoursPerCircle = hoursPerCircle;
        gDegreesPerHour = 360 / hoursPerCircle;
        gDegreeOffsetByHours = hoursPerCircle == 12 ? -90 : 90;

        int color = Color.rgb(0xff, 0x88, 0x00);

        //TEST code for emulator
        if(BuildConfig.DEBUG) {
            add(new ArcSegment(gDegreeOffsetByHours + 8 * gDegreesPerHour, 1.5f * gDegreesPerHour, gRadius, gThickness, color, Color.WHITE, "", gThickness * 0.66f, NORMAL_TYPEFACE, Color.WHITE));
            add(new ArcSegment(gDegreeOffsetByHours + 11 * gDegreesPerHour, 2f * gDegreesPerHour, gRadius, gThickness, color, Color.WHITE, "", gThickness * 0.66f, NORMAL_TYPEFACE, Color.WHITE));
            add(new ArcSegment(gDegreeOffsetByHours + 15 * gDegreesPerHour, 1f * gDegreesPerHour, gRadius, gThickness, color, Color.WHITE, "", gThickness * 0.66f, NORMAL_TYPEFACE, Color.WHITE));
            add(new ArcSegment(gDegreeOffsetByHours + 16 * gDegreesPerHour, 0.5f * gDegreesPerHour, gRadius, gThickness, color, Color.WHITE, "", gThickness * 0.66f, NORMAL_TYPEFACE, Color.WHITE));
            add(new ArcSegment(gDegreeOffsetByHours + 20 * gDegreesPerHour, 2f * gDegreesPerHour, gRadius, gThickness, color, Color.WHITE, "", gThickness * 0.66f, NORMAL_TYPEFACE, Color.WHITE));
        }
    }

    public void addCalendarEvents(List<CalendarEvent> events) {
        gArcs.clear();

        for(CalendarEvent event : events) {
            if(event.getAllDay()) {
                Log.d("AgendaRing", String.format(Locale.getDefault(), "event start, duration, name = %s, %s, %s",
                        hoursFromStartOfDay(event.getStart()), (event.getEnd()-event.getStart())/DateUtils.HOUR_IN_MILLIS, event.getTitle()));
            } else {
                float start = hoursFromStartOfDay(event.getStart());
                float duration = (event.getEnd()-event.getStart())/(1f*DateUtils.HOUR_IN_MILLIS);
                String title = ""; //event.getTitle();
                Log.d("AgendaRing", String.format(Locale.getDefault(), "event title, start, duration = %s, %s, %s", title, start, duration));
                int color = Integer.valueOf(event.getEventColor());
                add(new ArcSegment(gDegreeOffsetByHours + start*gDegreesPerHour, duration*gDegreesPerHour, gRadius, gThickness, color, Color.WHITE, title, gThickness*0.66f, NORMAL_TYPEFACE, Color.WHITE));
            }
        }
    }

    public static float hoursFromStartOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return (float) (millis - cal.getTimeInMillis())/ DateUtils.HOUR_IN_MILLIS;
    }

    @Override
    protected void onDraw(Canvas canvas, boolean ambientMode) {
        super.onDraw(canvas, ambientMode);
    }
}

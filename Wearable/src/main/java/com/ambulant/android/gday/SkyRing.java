package com.ambulant.android.gday;

import android.graphics.Canvas;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;

import com.ambulant.android.gday.models.WeatherEvent;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Sparse ring of segments for an agenda from the user's calendar
 */
public class SkyRing extends Ring {
    private float gDegreesPerHour;
    private float gDegreeOffsetByHours = 90; // degrees of rotation for the beginning: 90 for 24 hr clock, -90 for 12 hr clock

    private int clear_sky = Color.rgb(0x66, 0x98, 0xff);
    private int weather_rain = Color.rgb(0x40, 0x40, 0x40);
    private int weather_cloudy = Color.rgb(0xaa, 0xaa, 0xaa);

    /**
     * An Agenda sparse ring showing busy times around the clock
     * @param centerX the x position of the circle's center
     * @param centerY the y position of the circle's center
     * @param radius    the radius of the circle arcs are placed on from the center
     * @param thickness the thickness of the circle spreading closer and farther from the radius
     * @param hoursPerCircle the number of time divisions on the circle. 24 for 24 hours of time.
     */
    public SkyRing(float centerX, float centerY, float radius, float thickness, int hoursPerCircle) {
        super(centerX, centerY, radius, thickness);

        gDegreesPerHour = 360 / hoursPerCircle;
        gDegreeOffsetByHours = hoursPerCircle == 12 ? -90 : 90;


        //TEST code for emulator
//        add(new ArcSegment(gDegreeOffsetByHours + 6*gDegreesPerHour, 3f*gDegreesPerHour, gRadius, gThickness, weather_rain, weather_rain, "", 12, NORMAL_TYPEFACE, Color.WHITE));
//        add(new ArcSegment(gDegreeOffsetByHours + 9*gDegreesPerHour, 3f*gDegreesPerHour, gRadius, gThickness, weather_cloudy, weather_cloudy, "", 12, NORMAL_TYPEFACE, Color.WHITE));
//        add(new ArcSegment(gDegreeOffsetByHours + 12*gDegreesPerHour, 3f*gDegreesPerHour, gRadius, gThickness, clear_sky, clear_sky, "", 12, NORMAL_TYPEFACE, Color.WHITE));
//        add(new ArcSegment(gDegreeOffsetByHours + 15*gDegreesPerHour, 3f*gDegreesPerHour, gRadius, gThickness, clear_sky, clear_sky, "", 12, NORMAL_TYPEFACE, Color.WHITE));
//        add(new ArcSegment(gDegreeOffsetByHours + 18*gDegreesPerHour, 2f*gDegreesPerHour, gRadius, gThickness, clear_sky, clear_sky, "", 12, NORMAL_TYPEFACE, Color.WHITE));
    }

    public void setWeatherEvents(List<com.ambulant.android.gday.models.WeatherEvent> events) {
        gArcs.clear();

        for(WeatherEvent event : events) {
            float start = hoursFromStartOfDay(event.getStartMillis());
            float duration = (event.getEndMillis()-event.getStartMillis())/(1f*DateUtils.HOUR_IN_MILLIS);
            String title = ""; //event.getTitle();
            Log.d("WeatherRing", String.format(Locale.getDefault(), "event title, start, duration = %s, %s, %s", title, start, duration));
            int color = event.getPrecipColor();

            add(new ArcSegment(gDegreeOffsetByHours + start*gDegreesPerHour, duration*gDegreesPerHour, gRadius, gThickness, color, Color.TRANSPARENT, title, 12, NORMAL_TYPEFACE, Color.WHITE));
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
        if(!ambientMode) {
            super.onDraw(canvas, ambientMode);
        }
    }
}

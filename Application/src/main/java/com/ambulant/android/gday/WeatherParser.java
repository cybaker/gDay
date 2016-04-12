package com.ambulant.android.gday;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeatherParser {
    private static final String TAG = "WeatherParser";

    public WeatherParser() {}

    /**
     * Provide a list of the day's weather events on an hourly basis from weather.gov
     * @param url url for weather
     * @return list of weather events
     */
    public static List<WeatherEvent> getWeatherGovData(String url) {
        List<Integer> hours = new ArrayList<>();
        List<String> temps = new ArrayList<>();
        List<String> precips = new ArrayList<>();

        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout( 60 * (int) DateUtils.SECOND_IN_MILLIS).get();

            // This is specific to Weather.gov
            Elements tables = doc.select("table");
            for(Element table : tables) {
                Elements rows = table.select("tr");
                for(Element row : rows) { // look for 25 columns, this is the data table
                    Elements cols = row.select("td");
                    if(cols.size() == 25) {
                        for (Element col : cols) {
                            if (col.text().contains("Hour") && hours.size()<24) {
                                // Process Hour values
                                for (int i = 1; i < 25; i++) {
                                    hours.add(Integer.valueOf(cols.get(i).text()));
                                }
                            }
                            if (cols.text().contains("Temperature") && temps.size()<24) {
                                // Process Temperature values
                                for (int i = 1; i < 25; i++) {
                                    temps.add(cols.get(i).text());
                                }
                            }
                            if (cols.text().contains("Precipitation") && precips.size()<24) {
                                // Process Precipitation values
                                for (int i = 1; i < 25; i++) {
                                    precips.add(cols.get(i).text());
                                }
                            }
                        }
                    }
                }
                // Just need the first table that has values as it's the first 24 hours. Second table ignored
                if(!hours.isEmpty() && !temps.isEmpty() && !precips.isEmpty()) {
                    break;
                }
            }

            // Put together weather events
            if(!hours.isEmpty() && ((hours.size() == temps.size()) && (hours.size() == precips.size()))) {
                List<WeatherEvent> events = new ArrayList<WeatherEvent>();

                int index = 0;
                while(hours.get(index) > 0 && index <= 23) { // just loop through the hours until midnight
                    int hour = hours.get(index);

                    int temp = Integer.valueOf(temps.get(index));
                    int pop = Integer.valueOf(precips.get(index));

                    WeatherEvent event = new WeatherEvent("", todaysMillisByHour(hour), todaysMillisByHour(hour+1),
                            getColorOfTemperature(temp), getColorOfPrecipitation(pop));

                    events.add(event);

                    index++;
                }
                return events;
            }
        } catch (IOException e) {
            Log.d(TAG, "Could not fetch weather - i/o exception");
            // TODO - consider relaunching in 5 minutes as this is sometimes a socket timeout
        }

        return new ArrayList<WeatherEvent>(); // return something if no data
    }

    /**
     * Determines a color value given temperature in fahrenheit
     * @param tempf temperature in fahrenheit
     * @return int color
     */
    private static int getColorOfTemperature(int tempf)  {
        int rgb = 0;

            if (tempf >= 100) {
                rgb = 0xFFFFFF;
            } else if (tempf >= 95) {
                rgb = 0xFFCCCCCC;
            } else if (tempf >= 90 && tempf <=94) {
                rgb = 0xFFFF0000;
            } else if (tempf >= 80 && tempf <=89) {
                rgb = 0xFFFFA500;
            } else if (tempf >= 70 && tempf <=79) {
                rgb = 0xFF347C2C;
            } else if (tempf >= 60 && tempf <=69) {
                rgb = 0xFF43BFC7;
            } else if (tempf >= 50 && tempf <=59) {
                rgb = 0xFF43BFC7;
            } else if (tempf >= 40 && tempf <=49) {
                rgb = 0xFF56A5EC;
            } else if (tempf >= 30 && tempf <=39) {
                rgb = 0xFF2B65EC;
            } else if (tempf >= 20 && tempf <=29) {
                rgb = 0xFF0020C2;
            } else if (tempf <=19) {
                rgb = 0xFF800080;
            }

//        Log.d("WeatherParser", String.format(Locale.getDefault(), "Temp = %s:  RGB = %s, %s, %s", tempf, rgb & 0xFF, rgb>>8 & 0xFF, rgb>>16 & 0xFF));
        return Color.argb(0xff, rgb>>16 & 0xFF, rgb>>8 & 0xFF, rgb & 0xFF);
    }

    /**
     * Return a color representing pop
     * @param pop percentage of precipitation, 0 to 100
     * @return int color
     */
    private static int getColorOfPrecipitation(int pop) {
        int value = 0xFF * (100 - pop) / 100;
        value = Math.min(0xFF, Math.max(0x40, value)); // bound between 0x40 and 0xFF

        int out = 0xFF<<24 | value<<16 | value<<8 | value; // grey scale

//        Log.d("WeatherParser", String.format(Locale.getDefault(), "POP = %s, RGB = %s", pop, rgb));
        return out;
    }

    /**
     * return System millis of hour of current day
     * @param hourOfDay the hour of the day between 0.0 and 23.999;
     * @return
     */
    private static long todaysMillisByHour(float hourOfDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        long dayStartMillis = cal.getTimeInMillis();
        long offsetMillis = (long) (hourOfDay * DateUtils.HOUR_IN_MILLIS);
        return dayStartMillis + offsetMillis;
    }
}
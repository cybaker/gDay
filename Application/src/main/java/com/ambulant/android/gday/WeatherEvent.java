package com.ambulant.android.gday;

/**
 * Model WeatherEvent
 */
public class WeatherEvent {
    long startMillis = -1;
    long endMillis = -1;
    String title = "";
    int tempColor = 0;
    int precipColor = 0;

    public WeatherEvent() { }

    /**
     * A weather event model
     * @param startMillis
     * @param endMillis
     * @param title
     * @param tempColor
     * @param precipColor
     */
    public WeatherEvent(String title, long startMillis, long endMillis, int tempColor, int precipColor) {
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.precipColor = precipColor;
        this.tempColor = tempColor;
        this.title = title;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTempColor() {
        return tempColor;
    }

    public void setTempColor(int tempColor) {
        this.tempColor = tempColor;
    }

    public int getPrecipColor() {
        return precipColor;
    }

    public void setPrecipColor(int precipColor) {
        this.precipColor = precipColor;
    }
}

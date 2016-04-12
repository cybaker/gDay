package com.ambulant.android.gday.models;

/**
 * Model CalendarEvent
 */
public class CalendarEvent {
    long startMillis;
    long endMillis;
    String title;
    Boolean isAllDay;
    String eventColor;

    public long getStart() {
        return startMillis;
    }

    public void setStart(long beginVal) {
        this.startMillis = beginVal;
    }

    public long getEnd() {
        return endMillis;
    }

    public void setEnd(long endVal) {
        this.endMillis = endVal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getAllDay() {
        return isAllDay;
    }

    public void setAllDay(Boolean allDay) {
        isAllDay = allDay;
    }

    public String getEventColor() {
        return eventColor;
    }

    public void setEventColor(String eventColor) {
        this.eventColor = eventColor;
    }
}

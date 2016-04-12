package com.ambulant.android.gday;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * A receiver that triggers the WeatherService update
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent background = new Intent(context, WeatherService.class);
        startWakefulService(context, background);
    }
}
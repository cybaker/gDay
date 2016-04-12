package com.ambulant.android.gday;

/**
 * weather service for updating weather to the watch
 */

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.example.android.wearable.watchface.BuildConfig;
import com.example.android.wearable.watchface.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherService extends IntentService  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = WeatherService.this.getClass().getSimpleName();

    private final long ALARM_INTERVAL_NORMAL = 60 * DateUtils.MINUTE_IN_MILLIS;
    private final long ALARM_INTERVAL_SHORT = 5 * DateUtils.MINUTE_IN_MILLIS;
    private final int ALARM_PENDING_INTENT_ID = 1;

    GoogleApiClient mGoogleApiClient;

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        runFetcher();
    }

    //************ Google API support ***************
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    private void runFetcher() {
        final AsyncTask<Void, Void, Void> fetchTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                final LocationManager locationManager = (LocationManager) WeatherService.this.getSystemService(Context.LOCATION_SERVICE);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (lastKnownLocation == null) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
                } else {
                    getWeatherFromLatLon(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                }
                return null;
            }
        };

        fetchTask.execute();
    }

    private void getWeatherFromLatLon(double lat, double lon) {
        Log.d(TAG, "getWeatherFromLatLon");
        final String APIURL = "http://forecast.weather.gov/MapClick.php?lat=%f&lon=%f&FcstType=digital";
        String url = String.format(Locale.getDefault(), APIURL, lat, lon);

        List<WeatherEvent> list = WeatherParser.getWeatherGovData(url);

        if(!list.isEmpty()) {
            String json = new Gson().toJson(list);

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(ServiceConstants.PATH_WEATHER_INFO);
            putDataMapReq.getDataMap().putString(ServiceConstants.KEY_WEATHER_LIST, json);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

            Log.d(TAG, "Sent data to watch");
            setNextAlarm(ALARM_INTERVAL_NORMAL);
            sendTestNotification(true);
        } else {
            // No data received, try in a short period
            setNextAlarm(ALARM_INTERVAL_SHORT);
            sendTestNotification(false);
        }

        // disconnect before ending
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");
            getWeatherFromLatLon(location.getLatitude(), location.getLongitude());
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) { }
    };

    private void sendTestNotification(boolean success) {
        if(BuildConfig.DEBUG) {
            int notificationId = 101;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = sdf.format(new Date());

            String message = success ? "gDay weather updated " + time : "gDay weather failed at " + time;

            // Build intent for notification content
            Intent viewIntent = new Intent(this, gDayWatchFaceConfigActivity.class);
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(this, 0, viewIntent, 0);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_watch_white)
                            .setContentTitle("gDay weather update")
                            .setContentText(message)
                            .setContentIntent(viewPendingIntent);

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            // Build the notification and issues it with notification manager.
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    private void setNextAlarm(long futureMillis) {
        Intent alarm = new Intent(WeatherService.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(WeatherService.this, ALARM_PENDING_INTENT_ID, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + futureMillis, pendingIntent);
        Log.d(TAG, String.format(Locale.getDefault(), "next Alarm in %s millis", futureMillis));
    }
}

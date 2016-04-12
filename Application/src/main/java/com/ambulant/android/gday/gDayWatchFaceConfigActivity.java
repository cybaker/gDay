/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ambulant.android.gday;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.example.android.wearable.watchface.R;

public class gDayWatchFaceConfigActivity extends Activity {
    private final String TAG = gDayWatchFaceConfigActivity.this.getClass().getSimpleName();

    private final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 23455;
    private final int MY_PERMISSIONS_REQUEST_CALENDAR = 23456;

    private boolean locationPermissionOK = false;
    private boolean calendarPermissionOK = false;

    // Manual testing of the weather service fetch
    Button mSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analog_watch_face_config);

        mSend = (Button) findViewById(R.id.button_send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locationPermissionOK) {
                    startWeatherService();
                }
            }
        });

        checkLocationPermission();
        checkCalendarPermission();
    }

    /**
     * Starts the alarm service, wiping out the existing alarm
     */
    private void startWeatherService() {
        Intent background = new Intent(this, WeatherService.class);
        startService(background);
    }

    //*********** Permissions support **********
    /**
     * Check location permissions to fetch weather
     */
    private void checkLocationPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //FIXME for production

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            }
        } else {
            locationPermissionOK = true;
            startWeatherService();
        }
    }

    /**
     * Check location permissions to fetch weather
     */
    private void checkCalendarPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALENDAR)) {
                // not implemented
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        MY_PERMISSIONS_REQUEST_CALENDAR);
            }
        } else {
            calendarPermissionOK = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty. If manually turned
                // off by user, result is -1
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionOK = true;
                    startWeatherService();
                }
            case MY_PERMISSIONS_REQUEST_CALENDAR:
                // If request is cancelled, the result arrays are empty. If manually turned
                // off by user, result is -1
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    calendarPermissionOK = true;
                }
                return;
            default:
        }
    }
}

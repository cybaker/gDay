/*
 * Modified from AnalogWatchFaceService by AOSP
 */

package com.ambulant.android.gday;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.graphics.Palette;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;

import com.example.android.wearable.watchface.R;
import com.ambulant.android.gday.models.CalendarEvent;
import com.ambulant.android.gday.models.WeatherEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 24 watch face with a ticking second hand. In ambient mode, the minute and second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 */
public class gDayWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "AnalogWatchFaceService";

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private static int HOURS_PER_CIRCLE = 24;

    /*
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private static final int MSG_UPDATE_TIME = 0;

        private static final float HOUR_HAND_STROKE_WIDTH = 10f;
        private static final float SECOND_TICK_STROKE_WIDTH = 4f;
        private static final float HOUR_TICK_STROKE_WIDTH = 2f;

        private static final float AGENDA_CIRCLE_WIDTH = 24f;
        private static final float WEATHER_CIRCLE_WIDTH = 10f;
        private static final float TEMPERATURE_CIRCLE_WIDTH = 4f;
        private static final float CIRCLE_SEPARATION = 4f;

        private static final float HOUR_HAND_LENGTH = 30f;
        private static final float DIGITAL_HOUR_TEXT_SIZE = 75f;
        private static final float DIGITAL_MINUTE_TEXT_SIZE = 60f;
        private static final float DATE_TEXT_SIZE = 20f;

        private static final float BATTERY_CIRCLE_WIDTH = 2f;
        private static final float DATE_CIRCLE_WIDTH = 30f;

        private static final float HOUR_RADIUS = AGENDA_CIRCLE_WIDTH / 2;
        private static final float HOUR_SWEEP_DEGREES = 2f;

        private static final int SHADOW_RADIUS = 6;

        private boolean mMuteMode;

        private float mCenterX;
        private float mCenterY;

        // The draw objects
        private PercentRing gBatteryRing;
//        private PercentRing gHourRing;
        private DateRing gDateRing;
        private AgendaRing gAgendaRing;
        private Ticks gTicks;
        private ArcSegment gHourSegment;
        private SkyRing gSkyRing; // sky conditions
        private TemperatureRing gTemperatureRing;
        private TimeRing gTimeRing;

        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mHourHandColor;
        private int mHourTickColor;
        private int mTimeTextColor;
        private int mWatchHandShadowColor;

        private Paint mHourHandPaint;
        private Paint mHourTextPaint;
        private Paint mMinuteTextPaint;
        private Paint mDateTextPaint;
        private Paint mHourTickPaint;

        private Paint mBackgroundPaint;
        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;

        private boolean mAmbientMode;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        private Rect mPeekCardBounds = new Rect();

        /* Handler to update the time once a second in interactive mode. */
        private final Handler mSecondTimerHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {

                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "updating time");
                }
                invalidate();
                if (shouldTimerBeRunning()) {
                    long timeMs = System.currentTimeMillis();
                    long delayMs = INTERACTIVE_UPDATE_RATE_MS
                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                    mSecondTimerHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(gDayWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setHotwordIndicatorGravity(Gravity.LEFT | Gravity.BOTTOM)
                    .build());

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);

            /* Set defaults for colors */
            mHourHandColor = Color.YELLOW;
            mHourTickColor = Color.WHITE;
            mTimeTextColor = Color.WHITE;
            mWatchHandShadowColor = Color.BLACK;

            mHourHandPaint = new Paint();
            mHourHandPaint.setColor(mHourHandColor);
            mHourHandPaint.setStrokeWidth(HOUR_HAND_STROKE_WIDTH);
            mHourHandPaint.setAntiAlias(true);
            mHourHandPaint.setStrokeCap(Paint.Cap.ROUND);
            mHourHandPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mHourTextPaint = createTextPaint(mTimeTextColor, BOLD_TYPEFACE);
            mHourTextPaint.setTextSize(DIGITAL_HOUR_TEXT_SIZE);
            mMinuteTextPaint = createTextPaint(mTimeTextColor);
            mMinuteTextPaint.setTextSize(DIGITAL_MINUTE_TEXT_SIZE);
            mDateTextPaint = createTextPaint(mTimeTextColor);
            mDateTextPaint.setTextSize(DATE_TEXT_SIZE);

            mHourTickPaint = new Paint();
            mHourTickPaint.setColor(mHourTickColor);
            mHourTickPaint.setStrokeWidth(HOUR_TICK_STROKE_WIDTH);
            mHourTickPaint.setAntiAlias(true);
            mHourTickPaint.setStyle(Paint.Style.STROKE);
            mHourTickPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            /* Extract colors from background image to improve watchface style. */
            Palette.generateAsync(
                    mBackgroundBitmap,
                    new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            if (palette != null) {
                                if (Log.isLoggable(TAG, Log.DEBUG)) {
                                    Log.d(TAG, "Palette: " + palette);
                                }
                                mHourHandColor = palette.getLightVibrantColor(Color.YELLOW);
                                mHourTickColor = palette.getLightVibrantColor(Color.WHITE);
                                mTimeTextColor = palette.getLightVibrantColor(Color.WHITE);
                                mWatchHandShadowColor = palette.getDarkMutedColor(Color.BLACK);
                                updateWatchHandStyle();
                            }
                        }
                    });

            registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            googleApiClient = new GoogleApiClient.Builder(gDayWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        /**
         * Initialize all the arc segments to draw
         */
        private void initRings() {
            float agendaCircleRadius = mCenterX - AGENDA_CIRCLE_WIDTH/2;
            float skyCircleRadius = mCenterX - AGENDA_CIRCLE_WIDTH - CIRCLE_SEPARATION - WEATHER_CIRCLE_WIDTH/2;
            float tempCircleRadius = skyCircleRadius - CIRCLE_SEPARATION - WEATHER_CIRCLE_WIDTH/2 - TEMPERATURE_CIRCLE_WIDTH/2;
            float dateCircleRadius = tempCircleRadius - CIRCLE_SEPARATION - DATE_CIRCLE_WIDTH / 2;
            float timeWidth = AGENDA_CIRCLE_WIDTH + WEATHER_CIRCLE_WIDTH + TEMPERATURE_CIRCLE_WIDTH + 4*CIRCLE_SEPARATION;
            float timeCircleRadius = mCenterX - timeWidth*0.4f;

            // Ticks around the outside?
            float innerTickRadius = mCenterX - 10;
            float outerTickRadius = mCenterX;
            gTicks = new Ticks(mCenterX, mCenterY, innerTickRadius, outerTickRadius, HOURS_PER_CIRCLE, mHourTickPaint);

            gDateRing = new DateRing(gDayWatchFaceService.this, mCenterX, mCenterY, dateCircleRadius, DATE_CIRCLE_WIDTH);
            gAgendaRing = new AgendaRing(mCenterX, mCenterY, agendaCircleRadius, AGENDA_CIRCLE_WIDTH, HOURS_PER_CIRCLE);
            gSkyRing = new SkyRing(mCenterX, mCenterY, skyCircleRadius, WEATHER_CIRCLE_WIDTH, HOURS_PER_CIRCLE);
            gTemperatureRing = new TemperatureRing(mCenterX, mCenterY, tempCircleRadius, TEMPERATURE_CIRCLE_WIDTH, HOURS_PER_CIRCLE);

            gTimeRing = new TimeRing(mCenterX, mCenterY, timeCircleRadius, timeWidth);

            float batteryCircleRadius = 16; // small ring
            gBatteryRing = new PercentRing(mCenterX, mCenterY + 64, batteryCircleRadius, BATTERY_CIRCLE_WIDTH, "B");
            gBatteryRing.setColor(Color.GREEN);

            gHourSegment = new ArcSegment(0, HOUR_SWEEP_DEGREES, timeCircleRadius, timeWidth, Color.RED, Color.RED, null, 0, null, 0);
        }


        private Paint createTextPaint(int defaultInteractiveColor) {
            return createTextPaint(defaultInteractiveColor, NORMAL_TYPEFACE);
        }

        private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(defaultInteractiveColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onDestroy() {
            mSecondTimerHandler.removeMessages(MSG_UPDATE_TIME);
            unregisterReceiver(mBatteryReceiver);

            releaseGoogleApiClient();

            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);
            }

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            mHourTextPaint.setTypeface(mBurnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);
        }

        /**
         * Called once a minute in ambient mode
         */
        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            }
            mAmbientMode = inAmbientMode;

            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }


        private void updateWatchHandStyle(){
            mHourTextPaint.setTypeface(mAmbientMode ? NORMAL_TYPEFACE : BOLD_TYPEFACE);

            mHourHandPaint.setAntiAlias(!mAmbientMode);
            mHourTickPaint.setAntiAlias(!mAmbientMode);

            if (mAmbientMode){
                mHourHandPaint.setColor(Color.WHITE);
                mHourTickPaint.setColor(Color.WHITE);

                mHourHandPaint.clearShadowLayer();
                mHourTickPaint.clearShadowLayer();

            } else {
                mHourHandPaint.setColor(mHourHandColor);
                mHourTickPaint.setColor(mHourTickColor);

                mHourHandPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mHourTickPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourTextPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinuteTextPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            initRings();
        }

        boolean blink = true;
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "onDraw");
            }

            // Black background
            canvas.drawColor(Color.BLACK);

            // Draw the 3 rings first

            gAgendaRing.onDraw(canvas, mAmbientMode);
            gSkyRing.onDraw(canvas, mAmbientMode);
            gTemperatureRing.onDraw(canvas, mAmbientMode);

            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            final float seconds =
                    (cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 360 / 60;
            final float minutesRotation = cal.get(Calendar.MINUTE) * 360 / 60;
            final float hourHandOffset = minutesRotation / HOURS_PER_CIRCLE;
            final float hoursRotation = (cal.get(Calendar.HOUR_OF_DAY) * 360 / HOURS_PER_CIRCLE + hourHandOffset + 90);

            float hoursSinceMidnight = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f;
            gTimeRing.setTime(hoursSinceMidnight);
            gTimeRing.onDraw(canvas, mAmbientMode);

            gTicks.onDraw(canvas, mAmbientMode);

            gHourSegment.setStartDegrees(hoursRotation-HOUR_SWEEP_DEGREES);
            gHourSegment.onDraw(canvas, mCenterX, mCenterY, mAmbientMode);

            gBatteryRing.onDraw(canvas, mAmbientMode);
            gDateRing.onDraw(canvas, mAmbientMode);

            // Draw the hours.
            String hourString;
            boolean is24Hour = DateFormat.is24HourFormat(gDayWatchFaceService.this);
            if (is24Hour) {
                hourString = formatTwoDigitNumber(cal.get(Calendar.HOUR_OF_DAY));
            } else {
                int hour = cal.get(Calendar.HOUR);
                if (hour == 0) {
                    hour = 12;
                }
                hourString = formatTwoDigitNumber(hour);
            }

            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            if(mAmbientMode && (hourOfDay >= 21 || hourOfDay < 06)) {
                mHourTextPaint.setColor(Color.RED);
                mMinuteTextPaint.setColor(Color.RED);
                mDateTextPaint.setColor(Color.RED);
            } else {
                mHourTextPaint.setColor(Color.WHITE);
                mMinuteTextPaint.setColor(Color.WHITE);
                mDateTextPaint.setColor(Color.WHITE);
            }

            String minuteString = ":" + formatTwoDigitNumber(cal.get(Calendar.MINUTE));

            float mTimeYOffset = mCenterY;// + DIGITAL_HOUR_TEXT_SIZE *0.33f;
            float x = mCenterX;

            // Draw the hour
            float hourWidth = mHourTextPaint.measureText(hourString);
            canvas.drawText(hourString, x - hourWidth, mTimeYOffset , mHourTextPaint);

            // Draw the minutes with a separator
            canvas.drawText(minuteString, x, mTimeYOffset, mMinuteTextPaint);

            /* Draw rectangle behind peek card in ambient mode to improve readability. */
            if (mAmbientMode) {
                canvas.drawRect(mPeekCardBounds, mBackgroundPaint);
            }
        }

        private String formatTwoDigitNumber(int hour) {
            return String.format(Locale.getDefault(), "%02d", hour);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            fetchMeetings(visible);

            if (visible) {
                googleApiClient.connect();
                invalidate();
            } else {
                releaseGoogleApiClient();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mPeekCardBounds.set(rect);
        }

        /**
         * Starts/stops the {@link #mSecondTimerHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "updateTimer");
            }
            mSecondTimerHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mSecondTimerHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mSecondTimerHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbientMode;
        }


        //****************** Battery support ****************

        /**
         * Battery change receiver
         */
        private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context arg0, Intent intent)
            {
                int batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int color = Color.GREEN;
                if(batteryPercentage < 30) {
                    color = Color.YELLOW;
                } else if(batteryPercentage < 10) {
                    color = Color.RED;
                }
                gBatteryRing.setColor(color);
                gBatteryRing.setPercent(batteryPercentage);
                if(isVisible()) {
                    invalidate();
                }
                Log.d("BatteryReceiver", "Percentage = " + batteryPercentage);
            }
        };


        //************** Meeting support ***************
        static final int MSG_LOAD_MEETINGS = 0;
        private boolean mCalendarPermissionApproved = false;
        private boolean mIsCalendarReceiverRegistered;
        private AsyncTask<Void, Void, Integer> mLoadMeetingsTask;
        private List<CalendarEvent> gCalendarEvents = new ArrayList<>();


        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction())
                        && WearableCalendarContract.CONTENT_URI.equals(intent.getData())) {
                    mLoadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
                }
            }
        };

        /**
         * Register or unregister meetings listener based on visibility
         * @param visible
         */
        private void fetchMeetings(boolean visible) {
            if (visible) {
                // Enables app to handle 23+ (M+) style permissions.
                mCalendarPermissionApproved = ActivityCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;

                if (mCalendarPermissionApproved) {
                    IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
                    filter.addDataScheme("content");
                    filter.addDataAuthority(WearableCalendarContract.AUTHORITY, null);
                    registerReceiver(mBroadcastReceiver, filter);
                    mIsCalendarReceiverRegistered = true;

                    mLoadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
                }
            } else {
                if (mIsCalendarReceiverRegistered) {
                    unregisterReceiver(mBroadcastReceiver);
                    mIsCalendarReceiverRegistered = false;
                }
                mLoadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
            }
        }

        /** Handler to load the meetings once a minute in interactive mode. */
        final Handler mLoadMeetingsHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LOAD_MEETINGS:
                        cancelLoadMeetingTask();
                        if (mCalendarPermissionApproved) {
                            mLoadMeetingsTask = new LoadMeetingsTask();
                            mLoadMeetingsTask.execute();
                        }
                        break;
                }
            }
        };

        private void onMeetingsLoaded(int result) {
            if (result >= 0) {
                gAgendaRing.addCalendarEvents(gCalendarEvents);
                invalidate();
            }
        }

        private void cancelLoadMeetingTask() {
            if (mLoadMeetingsTask != null) {
                mLoadMeetingsTask.cancel(true);
            }
        }

        /**
         * Asynchronous task to load the meetings from the content provider and report the number of
         * meetings back via {@link #onMeetingsLoaded}.
         */
        private class LoadMeetingsTask extends AsyncTask<Void, Void, Integer> {
            private PowerManager.WakeLock mWakeLock;

            @Override
            protected Integer doInBackground(Void... voids) {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                mWakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, "AnalogWatchFaceWakeLock");
                mWakeLock.acquire();

                long begin = System.currentTimeMillis(); // start of the current day

                // This can only retrieve the next 24 hours of events, not even past events
                Uri.Builder builder =
                        WearableCalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, begin);
                ContentUris.appendId(builder, begin + DateUtils.DAY_IN_MILLIS);
                final Cursor cursor = getContentResolver().query(builder.build(),
                        null, null, null, null);

                int numMeetings;
                if(cursor != null) {
                    numMeetings = cursor.getCount();
                    Log.d(TAG, "Num meetings: " + numMeetings);

                    gCalendarEvents.clear();
                    while (cursor.moveToNext()) {
                        long beginVal = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.BEGIN));
                        long endVal = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.END));
                        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Instances.TITLE));
                        Boolean isAllDay = !cursor.getString(cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)).equals("0");
                        String eventColor = cursor.getString(cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR));

                        CalendarEvent newEvent = new CalendarEvent();
                        newEvent.setTitle(title);
                        newEvent.setStart(beginVal);
                        newEvent.setEnd(endVal);
                        newEvent.setAllDay(isAllDay);
                        newEvent.setEventColor(eventColor);
                        gCalendarEvents.add(newEvent);
                    }

                    cursor.close();
                }
                else {
                    numMeetings = -1;
                }
                return numMeetings;
            }

            @Override
            protected void onPostExecute(Integer result) {
                releaseWakeLock();
                onMeetingsLoaded(result);
            }

            @Override
            protected void onCancelled() {
                releaseWakeLock();
            }

            private void releaseWakeLock() {
                if (mWakeLock != null) {
                    mWakeLock.release();
                    mWakeLock = null;
                }
            }
        }

        // Connections
        private GoogleApiClient googleApiClient;

        private void releaseGoogleApiClient() {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(googleApiClient, onDataChangedListener);
                googleApiClient.disconnect();
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "connected GoogleAPI");

            Wearable.DataApi.addListener(googleApiClient, onDataChangedListener);
            Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(onConnectedResultCallback);
        }

        private final DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        DataItem item = event.getDataItem();
                        processConfigurationFor(item);
                    }
                }

                dataEvents.release();
                invalidateIfNecessary();
            }
        };

        private void processConfigurationFor(DataItem item) {
            if (ServiceConstants.PATH_WEATHER_INFO.equals(item.getUri().getPath())) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (dataMap.containsKey(ServiceConstants.KEY_WEATHER_LIST)) {
                    String listString = dataMap.getString(ServiceConstants.KEY_WEATHER_LIST);
                    Log.d(TAG, "Weather list update received: "+ listString);

                    List<WeatherEvent> events = new Gson().fromJson(listString, new TypeToken<List<WeatherEvent>>(){}.getType());
                    gSkyRing.setWeatherEvents(events);
                    gTemperatureRing.setWeatherEvents(events);
                }
                invalidate();
            }
        }

        private final ResultCallback<DataItemBuffer> onConnectedResultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem item : dataItems) {
                    processConfigurationFor(item);
                }

                dataItems.release();
                invalidateIfNecessary();
            }
        };

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "suspended GoogleAPI");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "connectionFailed GoogleAPI");
        }

        private void invalidateIfNecessary() {
            if (isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }
    }
}

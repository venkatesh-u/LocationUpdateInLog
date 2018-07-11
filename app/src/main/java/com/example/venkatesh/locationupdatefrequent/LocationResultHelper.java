/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.example.venkatesh.locationupdatefrequent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

/**
 * Class to process location results.
 */
class LocationResultHelper {
private static String TAG = LocationResultHelper.class.getSimpleName();
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";

    final private static String PRIMARY_CHANNEL = "default";


    private Context mContext;
    //private List<Location> mLocations;
    private Location mLocation;
    private NotificationManager mNotificationManager;

    LocationResultHelper(Context context, Location location) {
        Log.d(TAG,"LocationResultHelper Constructor");
        mContext = context;
        mLocation = location;

        /*NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(channel);*/
    }

    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     */
    public static String getLocationResultTitle() {
//        Log.d(TAG,"getLocationResultTitle");

        String numLocationsReported = null;
                //= mContext.getResources().getQuantityString(
                //R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    public String getLocationResultText() {
//        Log.d(TAG,"getLocationResultText");

        if (mLocation == null) {
            return mContext.getString(R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(mLocation.getLatitude());
        sb.append(", ");
        sb.append(mLocation.getLongitude());
        sb.append(")");
        return sb.toString();
    }

    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     */
    void saveResults() {
        Log.d(TAG,"saveResults");

        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle() + "\n" +
                        getLocationResultText())
                .apply();
    }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {
        Log.d(TAG,"getSavedLocationResult");

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            Log.d(TAG,"getNotificationManager");
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * Displays a notification with the location results.
     */
    void showNotification() {
        Intent notificationIntent = new Intent(mContext, GeoFencingDemo.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(GeoFencingDemo.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(mContext)
                .setContentTitle(getLocationResultTitle())
                .setContentText(getLocationResultText())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(notificationPendingIntent);

        getNotificationManager().notify(0, notificationBuilder.build());

        /*
        // Creating an intent for broadcastreceiver
        Intent broadcastIntent = new Intent(Constants.BROADCAST_ACTION);
        // Attaching data to the intent
        broadcastIntent.putExtra(Constants.EXTENDED_DATA_STATUS, getLocationResultTitle());
        // Sending the broadcast
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
        */
    }
}

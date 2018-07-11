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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;


/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG , "onReceive");
        if (intent != null) {
            Log.d(TAG , "onReceive entered if");
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                Log.d(TAG , "onReceive entered ACTION_PROCESS_UPDATES");

                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Log.d(TAG , "onReceive entered result");
                    List<Location> locations = result.getLocations();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations.get(0));
                    // Save the location data to SharedPreferences.
                    locationResultHelper.saveResults();
                    // Show notification with the location data.
                    //locationResultHelper.showNotification();
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));

//                    Stock stk = new Stock();
//                    stk.setSymbol(String.valueOf(locations.get(0).getLatitude())
//                            + ", "+ String.valueOf(locations.get(0).getLongitude()));

//                    performSubmit(stk, context);

                    // Creating an intent for broadcastreceiver
                    Intent broadcastIntent = new Intent(Constants.BROADCAST_ACTION);
                    // Attaching data to the intent
                    broadcastIntent.putExtra(Constants.EXTENDED_DATA_STATUS, result);
                    // Sending the broadcast
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                }
            }
        }
    }

//
//    private void performSubmit(Stock p, Context context) {
//
//        UserRegBean bean = new UserRegBean();
//        bean.email = "work3@mailinator.com";
//        bean.password = "12345678";
//        bean.device_id = Settings.Secure.getString(MyApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
//        bean.device_token = PreferencesData.getRegistrationId(context);
//        bean.area_code = p.getSymbol();
//
//        Call<ResponseBody> cl;
//        MainNetworkService service = MyApplication.getSerivce();
//        cl = getCallInstance(service, bean);
//        cl.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
//                Log.d("Data: success, ", response.toString());
//            }
//            @Override
//            public void onFailure(Throwable t) {
//                Log.d("Data: failure, ", t.toString());
//
//            }
//        });
//        ///new task().execute();
//    }
//
//    public Call<ResponseBody> getCallInstance(MainNetworkService service, UserRegBean bean) {
//        return  service.userSignin("users/sign_in", bean.device_token, bean);
//    }

}

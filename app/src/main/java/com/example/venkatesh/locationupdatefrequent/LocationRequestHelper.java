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

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;


class LocationRequestHelper {

    final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    private static String TAG = LocationRequestHelper.class.getSimpleName();

    static void setRequesting(Context context, boolean value) {
        Log.d(TAG,  "setRequesting");
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static boolean getRequesting(Context context) {
        Log.d(TAG,  "getRequesting");

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }
}

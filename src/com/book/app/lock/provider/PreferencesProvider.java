/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.book.app.lock.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferencesProvider {

    private static SharedPreferences sp;
    private static final String DEFAULT_APP = "com.book.app.lock";

    public static SharedPreferences getSharedPreferences(Context context) {
        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sp;
    }

    public PreferencesProvider() {
    }

    public static void setDefaultPackage(Context context, String appId) {
        getSharedPreferences(context).edit().putString("default_app", appId)
                .commit();
    }

    public static String getDefaultPackage(Context context) {
        return getSharedPreferences(context).getString("default_app",
                DEFAULT_APP);
    }
}
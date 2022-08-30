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

package com.book.app.lock;

import com.book.app.lock.ChooseLockPattern.ChooseLockPatternFragment;
import com.book.app.lock.provider.AppLockData;
import com.book.app.lock.provider.PreferencesProvider;
import book.support.v7.app.ActionBarActivity;
import book.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConfirmLockActivity extends PreferenceActivity {
    private String mAppName = "";
    private AppLockData mAppLockData;
    private static SharedPreferences sp;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                ConfirmLockActivityFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
    protected boolean isValidFragment(String fragmentName) {
        if (ConfirmLockActivityFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppLockData = new AppLockData(getApplicationContext());
        Intent intent = getIntent();
        mAppName = intent.getStringExtra("APP_PACKAGE_NAME");
        PreferencesProvider
                .setDefaultPackage(getApplicationContext(), mAppName);
        Log.d("liuwenshuai", "ConfirmLockActivityFragment->appName:" + mAppName);
        //((ActionBarActivity)getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
    }

    public static class ConfirmLockActivityFragment extends PreferenceFragment {
        private static final int CONFIRM_EXISTING_REQUEST = 100;

        public static final String MINIMUM_QUALITY_KEY = "minimum_quality";
        public final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";
        private boolean mPasswordConfirmed = false;
        private boolean mWaitingForConfirmation = false;

        public ConfirmLockActivityFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(
                    this.getActivity(), this);
            if (helper.launchConfirmationActivity(CONFIRM_EXISTING_REQUEST,
                    null, null)) {
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mPasswordConfirmed) {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = super
                    .onCreateView(inflater, container, savedInstanceState);
            return v;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            mWaitingForConfirmation = false;
            if (requestCode == CONFIRM_EXISTING_REQUEST
                    && resultCode == Activity.RESULT_OK) {
                mPasswordConfirmed = true;
                Log.d("liuwenshuai",
                        "confirmlockactivity->CONFIRM_EXISTING_REQUEST");
            } else {
                Log.d("liuwenshuai", "confirmlockactivity...other...");
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }
    }
}

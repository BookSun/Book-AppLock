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

import book.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import book.provider.ExtraSettings;
import book.support.v7.app.ActionBarActivity;
public class bookAppLockSettings extends PreferenceActivity {
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                bookAppLockSettingsFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.app_lock_settings);
        // actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (bookAppLockSettingsFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

    public static class bookAppLockSettingsFragment extends PreferenceFragment {

        private SwitchPreference mAppLockSwitchPreference;
        private SwitchPreference mPatternVisiblePreference;
        private SwitchPreference mPatternFeedBackPreference;
        private static final String APP_LOCK_KEY = "app_lock_switch";
        private static final String APP_LOCK_PASSWORD = "app_lock_password";
        private static final String PATTERN_VISIBLE = "pattern_view_visible";
        private static final String PATTERN_FEED_BACK = "pattern_feed_back";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.book_applock_settings);
            mAppLockSwitchPreference = (SwitchPreference) findPreference(APP_LOCK_KEY);
            mPatternVisiblePreference = (SwitchPreference) findPreference(PATTERN_VISIBLE);
            mPatternFeedBackPreference = (SwitchPreference) findPreference(PATTERN_FEED_BACK);
        }

        @Override
        public void onResume() {
            super.onResume();
            updatePreference();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = super
                    .onCreateView(inflater, container, savedInstanceState);
            return v;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            // TODO Auto-generated method stub
            if (preference == mAppLockSwitchPreference) {
                boolean isChecked = mAppLockSwitchPreference.isChecked();
                setPreference(isChecked);
            } else if (preference.getKey().equals(APP_LOCK_PASSWORD)) {
                Intent newIntent = new Intent();
                newIntent.setClassName("com.book.app.lock",
                        "com.book.app.lock.bookChooseLockPattern");
                newIntent.putExtra("com.book.app.lock.MODIFY_PASSWORD",
                        "modify_password");
                getActivity().startActivity(newIntent);
            } else if (preference == mPatternVisiblePreference) {
                boolean isChecked = mPatternVisiblePreference.isChecked();
                setPatternVisible(isChecked);
            } else if (preference == mPatternFeedBackPreference) {
                boolean isChecked = mPatternFeedBackPreference.isChecked();
                setPatternFeedBack(isChecked);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void updatePreference() {
            boolean lockValue = Settings.System.getInt(getActivity()
                    .getContentResolver(),
                    ExtraSettings.System.book_APPLOCK_ENABLED, 1) == 1 ? true
                    : false;
            boolean patternVisible = Settings.System.getInt(getActivity()
                    .getContentResolver(), "book_applock_pattern_visible", 1) == 1 ? true
                    : false;
            boolean patternFeedBack = Settings.System.getInt(getActivity()
                    .getContentResolver(), "book_applock_feedback", 1) == 1 ? true
                    : false;
            if (mAppLockSwitchPreference != null) {
                mAppLockSwitchPreference.setChecked(lockValue);
            }
            if (mPatternFeedBackPreference != null) {
                mPatternFeedBackPreference.setChecked(patternFeedBack);
            }
            if (mPatternVisiblePreference != null) {
                mPatternVisiblePreference.setChecked(patternVisible);
            }
        }

        private void setPreference(boolean isChecked) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    ExtraSettings.System.book_APPLOCK_ENABLED, isChecked ? 1
                            : 0);
        }

        private void setPatternVisible(boolean isChecked) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    "book_applock_pattern_visible", isChecked ? 1 : 0);
        }

        private void setPatternFeedBack(boolean isChecked) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    "book_applock_feedback", isChecked ? 1 : 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

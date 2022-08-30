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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.book.app.lock.ChangePassWord.ChangePassWordFragment;
import com.book.app.lock.provider.AppLockData;
import com.book.app.lock.provider.PreferencesProvider;
import com.android.internal.widget.LockPatternUtils;
import book.support.v7.app.ActionBarActivity;
import book.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.StaticLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ChooseLockGeneric extends PreferenceActivity {
    private String mAppName = "";
    private AppLockData mAppLockData;
    private static SharedPreferences sp;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                ChooseLockGenericFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockGenericFragment.class.getName().equals(fragmentName))
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
        //((ActionBarActivity)getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
    }

    public static class ChooseLockGenericFragment extends PreferenceFragment {
        private static final int MIN_PASSWORD_LENGTH = 4;
        private static final String KEY_UNLOCK_BACKUP_INFO = "unlock_backup_info";
        private static final String KEY_UNLOCK_SET_PASSWORD = "unlock_set_password";
        private static final String KEY_UNLOCK_SET_PATTERN = "unlock_set_pattern";
        private static final String KEY_UNLOCK_SET_SCREEN = "unlock_screen_lock";
        private static final int CONFIRM_EXISTING_REQUEST = 100;
        private static final int FALLBACK_REQUEST = 101;
        private static final String PASSWORD_CONFIRMED = "password_confirmed";
        private static final String CONFIRM_CREDENTIALS = "confirm_credentials";
        private static final String WAITING_FOR_CONFIRMATION = "waiting_for_confirmation";
        private static final String FINISH_PENDING = "finish_pending";
        public static final String MINIMUM_QUALITY_KEY = "minimum_quality";
        public final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";

        private static final boolean ALWAY_SHOW_TUTORIAL = true;

        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private LockPatternUtilsHelper mLockPatternUtilsHelper;
        private LockPatternUtils mLockPatternUtils;
        private boolean mPasswordConfirmed = false;
        private boolean mWaitingForConfirmation = false;
        private boolean mFinishPending = false;
        private DevicePolicyManager mDPM;

        public ChooseLockGenericFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mDPM = (DevicePolicyManager) getActivity().getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(
                    this.getActivity());
            mLockPatternUtilsHelper = new LockPatternUtilsHelper(getActivity());
            mLockPatternUtils = new LockPatternUtils(getActivity());
            // Defaults to needing to confirm credentials
            final boolean confirmCredentials = getActivity().getIntent()
                    .getBooleanExtra(CONFIRM_CREDENTIALS, true);
            mPasswordConfirmed = !confirmCredentials;

            if (savedInstanceState != null) {
                mPasswordConfirmed = savedInstanceState
                        .getBoolean(PASSWORD_CONFIRMED);
                mWaitingForConfirmation = savedInstanceState
                        .getBoolean(WAITING_FOR_CONFIRMATION);
                mFinishPending = savedInstanceState.getBoolean(FINISH_PENDING);
            }

            if (mPasswordConfirmed) {
                updatePreferencesOrFinish();
            } else if (!mWaitingForConfirmation) {
                ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(
                        this.getActivity(), this);
                if (!helper.launchConfirmationActivity(
                        CONFIRM_EXISTING_REQUEST, null, null)) {
                    mPasswordConfirmed = true; // no password set, so no need to
                                               // confirm
                    updatePreferencesOrFinish();
                } else {
                    mWaitingForConfirmation = true;
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mFinishPending) {
                mFinishPending = false;
                getActivity().finish();
            }
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            final String key = preference.getKey();
            boolean handled = true;
            if (KEY_UNLOCK_SET_PATTERN.equals(key)) {
                updateUnlockMethodAndFinish(
                        DevicePolicyManager.PASSWORD_QUALITY_SOMETHING, false);
            } else if (KEY_UNLOCK_SET_PASSWORD.equals(key)) {
                updateUnlockMethodAndFinish(
                        DevicePolicyManager.PASSWORD_QUALITY_NUMERIC, false);
            } else if (KEY_UNLOCK_SET_SCREEN.equals(key)) {
                updateUnlockMethodAndFinish(
                        DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED, false);
            } else {
                handled = false;
            }
            return handled;
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
                updatePreferencesOrFinish();
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(PASSWORD_CONFIRMED, mPasswordConfirmed);
            outState.putBoolean(WAITING_FOR_CONFIRMATION,
                    mWaitingForConfirmation);
            outState.putBoolean(FINISH_PENDING, mFinishPending);
        }

        private void updatePreferencesOrFinish() {
            Log.d("liuwenshuai", "updatePreferencesOrFinish...");
            Intent intent = getActivity().getIntent();
            int quality = intent.getIntExtra(
                    LockPatternUtils.PASSWORD_TYPE_KEY, -1);
            if (quality == -1) {
                quality = intent.getIntExtra(MINIMUM_QUALITY_KEY, -1);
                final PreferenceScreen prefScreen = getPreferenceScreen();
                if (prefScreen != null) {
                    prefScreen.removeAll();
                }
                addPreferencesFromResource(R.xml.security_settings_picker);
            } else {
                updateUnlockMethodAndFinish(quality, false);
            }
        }
        
        public boolean getPasswordEnable() {
            boolean passwordEnable = mLockPatternUtils.isLockPasswordEnabled()
                    && mLockPatternUtils.savedPasswordExists();
            if (passwordEnable) {
                return true;
            } else {
                return false;
            }
        }

        public boolean getPatternEnable() {
            boolean patternEnable = mLockPatternUtils.isLockPatternEnabled()
                    && mLockPatternUtils.savedPatternExists();
            if (patternEnable) {
                return true;
            } else {
                return false;
            }
        }

        void updateUnlockMethodAndFinish(int quality, boolean disabled) {
            if (!mPasswordConfirmed) {
                throw new IllegalStateException(
                        "Tried to update password without confirming it");
            }

            if (quality == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
                Intent intent = new Intent();
                intent.setClassName("com.book.app.lock",
                        "com.book.app.lock.ChooseLockPassword");
                intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
                intent.putExtra(CONFIRM_CREDENTIALS, false);
                mFinishPending = true;
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                getActivity().finish();
            } else if (quality == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
                Intent intent = new Intent();
                intent.setClassName("com.book.app.lock",
                        "com.book.app.lock.ChooseLockPattern");
                intent.putExtra("key_lock_method", "pattern");
                intent.putExtra(CONFIRM_CREDENTIALS, false);
                mFinishPending = true;
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                getActivity().finish();
            } else if (quality == DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) {
                if (getPatternEnable()) {
                    String newPath = mChooseLockSettingsHelper.utils()
                            .getNewPatternPath();
                    String oldPath = mLockPatternUtilsHelper
                            .getLockPatternFilename();
                    mLockPatternUtilsHelper.copyFile(oldPath, newPath);
                    mChooseLockSettingsHelper.utils().setLong(
                            PASSWORD_TYPE_KEY,
                            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
                    Toast.makeText(getActivity(),
                            R.string.pattern_screen_password,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else if (getPasswordEnable()) {
                    String newPath = mChooseLockSettingsHelper.utils()
                            .getNewPasswordPath();
                    String oldPath = mLockPatternUtilsHelper
                            .getLockPasswordFilename();
                    mLockPatternUtilsHelper.copyFile(oldPath, newPath);
                    mChooseLockSettingsHelper.utils().setLong(
                            PASSWORD_TYPE_KEY,
                            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
                    Toast.makeText(getActivity(),
                            R.string.password_screen_password,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), R.string.no_screen_password,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

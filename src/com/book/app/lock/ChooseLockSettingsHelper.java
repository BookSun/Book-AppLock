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

import book.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class ChooseLockSettingsHelper {

    static final String EXTRA_KEY_PASSWORD = "password";

    private LockPatternUtils mLockPatternUtils;
    private Activity mActivity;
    private Fragment mFragment;
    private Context mContext;

    public ChooseLockSettingsHelper(Activity activity) {
        mActivity = activity;
        mLockPatternUtils = new LockPatternUtils(activity);
    }

    public ChooseLockSettingsHelper(Context context) {
        mContext = context;
        mLockPatternUtils = new LockPatternUtils(context);
    }

    public ChooseLockSettingsHelper(Activity activity, Fragment fragment) {
        this(activity);
        mFragment = fragment;
    }

    public LockPatternUtils utils() {
        return mLockPatternUtils;
    }

    boolean launchConfirmationActivity(int request, CharSequence message,
            CharSequence details) {
        boolean launched = false;
        switch (mLockPatternUtils.getKeyguardStoredPasswordQuality()) {
        case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
            launched = confirmPattern(request, message, details);
            break;
        case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            launched = confirmPassword(request);
            break;
        }
        return launched;
    }
    private boolean confirmPattern(int request, CharSequence message,
            CharSequence details) {
        if (!mLockPatternUtils.savedPatternExists()) {
            return false;
        }
        final Intent intent = new Intent();

        intent.setClassName("com.book.app.lock",
                "com.book.app.lock.ConfirmLockPattern");
        if (mFragment != null) {
            mFragment.startActivityForResult(intent, request);
        } else if (mActivity != null) {
            mActivity.startActivityForResult(intent, request);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
        return true;
    }

    private boolean confirmPassword(int request) {
        if (!mLockPatternUtils.savedPasswordExists())
            return false;
        final Intent intent = new Intent();
        intent.setClassName("com.book.app.lock",
                "com.book.app.lock.ConfirmLockPassword");
        if (mFragment != null) {
            mFragment.startActivityForResult(intent, request);
        } else if (mActivity != null) {
            mActivity.startActivityForResult(intent, request);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
        return true;
    }

}

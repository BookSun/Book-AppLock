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
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import book.support.v7.app.ActionBarActivity;
import android.app.ActivityManagerNative;
import android.os.ServiceManager;

public final class LockPatternUtilsHelper {

    private LockPatternUtils mLockPatternUtils;
    private Activity mActivity;
    private Fragment mFragment;
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String LOCK_PATTERN_FILE = "gesture.key";
    private static final String LOCK_PASSWORD_FILE = "password.key";

    public LockPatternUtilsHelper(Activity activity) {
        mActivity = activity;
        mLockPatternUtils = new LockPatternUtils(activity);
    }

    public LockPatternUtilsHelper(Activity activity, Fragment fragment) {
        this(activity);
        mFragment = fragment;
    }
 
    public LockPatternUtils utils() {
        return mLockPatternUtils;
    }

    public String getLockPatternFilename() {
        String dataSystemDirectory = android.os.Environment.getDataDirectory()
                .getAbsolutePath() + SYSTEM_DIRECTORY;

        return dataSystemDirectory + LOCK_PATTERN_FILE;
    }

    public String getLockPasswordFilename() {
        String dataSystemDirectory = android.os.Environment.getDataDirectory()
                .getAbsolutePath() + SYSTEM_DIRECTORY;
        return dataSystemDirectory + LOCK_PASSWORD_FILE;

    }
   
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

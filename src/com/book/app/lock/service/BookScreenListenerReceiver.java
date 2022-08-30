package com.book.app.lock.service;

import java.util.ArrayList;
import java.util.List;

import com.book.app.lock.provider.bookAppLockHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;

public class bookScreenListenerReceiver extends BroadcastReceiver {

    private static final String LOCK_VALUE = "locked";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        bookAppLockHelper mLockDB = new bookAppLockHelper(context);
        List<String> mLockAppPackage = new ArrayList<String>();
        IActivityManager mAm = ActivityManagerNative.getDefault();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if ((Intent.ACTION_BOOT_COMPLETED).equals(action)) {
            context.startService(new Intent(context, bookAppLockService.class));
            mLockAppPackage = mLockDB.getAllLockedPackName(LOCK_VALUE);
            if (mLockAppPackage != null) {
                // for (String appPackage : mLockAppPackage) {
                //     try {
                //         mAm.addAppLockControlPackage(appPackage);
                //     } catch (RemoteException e) {
                //         // TODO: handle exception
                //     }
                // }
            }
        }
    }

}

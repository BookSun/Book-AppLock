package com.book.app.lock.application;

import com.book.themes.ThemeClientApplication;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class bookAppLockApplication extends ThemeClientApplication {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {
                // TODO Auto-generated method stub
                Log.d("liuwenshuai", "onActivityStopped....");
                if (activity.getClass().getName()
                        .equals("com.book.app.lock.bookConfirmLockPattern")) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity,
                    Bundle outState) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onActivityResumed(Activity activity) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onActivityPaused(Activity activity) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onActivityCreated(Activity activity,
                    Bundle savedInstanceState) {
                // TODO Auto-generated method stub

            }
        });
        super.onCreate();
    }

}

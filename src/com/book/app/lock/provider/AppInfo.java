package com.book.app.lock.provider;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable appIcon;
    private String appName;
    private String packName;
    private boolean isSystemApp;
    private boolean isActive;
    private int appNumber;

    public Drawable getIcon() {
        return appIcon;
    }

    public void setIcon(Drawable icon) {
        this.appIcon = icon;
    }

    public String getAppname() {
        return appName;
    }

    public void setAppname(String appname) {
        this.appName = appname;
    }

    public String getPackname() {
        return packName;
    }

    public void setPackname(String packname) {
        this.packName = packname;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean isSystemApp) {
        this.isSystemApp = isSystemApp;
    }

    public boolean getActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setAppNumber(int number) {
        this.appNumber = number;
    }

    public int getAppNumber() {
        return appNumber;
    }
}

package com.book.app.lock.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import com.book.themes.ThemeManager;

public class AppInfoProvider {

    private Context mContext;
    private AppLockData mAppLockData;
    private PackageManager packManager;
    private bookAppLockHelper mbookAppLockData;
    private HashSet<String> mPackageHashSet = new HashSet<String>();
    private final Comparator<AppInfo> mComparator = new Comparator<AppInfo>() {

        public int compare(AppInfo appInfo1, AppInfo appInfo2) {
            // TODO Auto-generated method stub
            return this.compare(appInfo1.getActive(), appInfo2.getActive());
        }

        public int compare(Boolean b1, Boolean b2) {
            return (b1.equals(b2) ? 0 : (b2.booleanValue() == true ? 1 : -1));
        }
    };

    public AppInfoProvider(Context context) {
        mContext = context;
        packManager = context.getPackageManager();
        mAppLockData = new AppLockData(context);
        mbookAppLockData = new bookAppLockHelper(context);
    }

    public List<AppInfo> getAllApps() {
        List<AppInfo> appinfos = new ArrayList<AppInfo>();
        List<PackageInfo> packinfos = packManager
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo info : packinfos) {
            AppInfo myApp = new AppInfo();
            String packname = info.packageName;
            if (!mAppLockData.find(packname))
                continue;
            myApp.setPackname(packname);
            ApplicationInfo appinfo = info.applicationInfo;
            Drawable icon = ThemeManager.getInstance(mContext).loadIcon(appinfo);//appinfo.loadIcon(packManager);
            myApp.setIcon(icon);
            String appname = appinfo.loadLabel(packManager).toString();
            myApp.setAppname(appname);
            appinfos.add(myApp);
        }
        return appinfos;
    }

    public List<AppInfo> getAllNeedLockApps() {
        List<AppInfo> appinfos = new ArrayList<AppInfo>();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> infoList = packManager.queryIntentActivities(
                mainIntent, 0);
        Collections.sort(infoList, new ResolveInfo.DisplayNameComparator(
                packManager));
        if (infoList != null) {
            for (ResolveInfo info : infoList) {
                AppInfo myApp = new AppInfo();
                String packname = info.activityInfo.packageName;
                if (mPackageHashSet.contains(packname)) {
                    continue;
                }
                if (mbookAppLockData.findLockApp(packname)) {
                    myApp.setActive(true);
                } else {
                    myApp.setActive(false);
                }
                myApp.setPackname(packname);
                Drawable icon = ThemeManager.getInstance(mContext).loadIcon(info);//info.loadIcon(packManager);
                myApp.setIcon(icon);
                String appname = info.loadLabel(packManager).toString();
                myApp.setAppname(appname);
                appinfos.add(myApp);
                mPackageHashSet.add(packname);
            }
        }
        return appinfos;
    }

    public List<AppInfo> getSortLockApps() {
        List<AppInfo> appinfos = new ArrayList<AppInfo>();
        appinfos = getAllNeedLockApps();
        Collections.sort(appinfos, mComparator);
        return appinfos;
    }

    public boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }
}

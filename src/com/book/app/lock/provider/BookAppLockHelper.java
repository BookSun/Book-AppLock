package com.book.app.lock.provider;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class bookAppLockHelper {
    private AppLockDBHelper appLockHelper;
    private static final String TABLE = "booklock";
    private static final String COLUMN_APPID = "packname";
    private static final String COLUMN_VALUE = "value";

    public bookAppLockHelper(Context context) {
        appLockHelper = new AppLockDBHelper(context);
    }

    public List<String> getAllLockedPackName(String lockValue) {
        SQLiteDatabase db = appLockHelper.getReadableDatabase();
        List<String> packnames = new ArrayList<String>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select packname from booklock where value=?",
                    new String[] { lockValue });
            while (cursor.moveToNext()) {
                String packname = cursor.getString(0);
                packnames.add(packname);
            }
            cursor.close();
            db.close();
        }
        return packnames;
    }
    public int getLockedAppNumber(String lockValue) {
        int appNumber = 0;
        SQLiteDatabase db = appLockHelper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select packname from booklock where value=?",
                    new String[] { lockValue });
            while (cursor.moveToNext()) {
                appNumber ++;
            }
            cursor.close();
            db.close();
        }
        return appNumber;
    }

    public void addLLockedPackageName(String value, String appId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APPID, appId);
        cv.put(COLUMN_VALUE, value);
        SQLiteDatabase db = appLockHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE, COLUMN_APPID + "=?",
                    new String[] { appId });
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    public boolean findLockApp(String packName) {
        boolean result = false;
        SQLiteDatabase db = appLockHelper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(
                    "select packname from booklock where packname=?",
                    new String[] { packName });
            if (cursor.moveToNext()) {
                result = true;
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    public void delete(String packname) {
        SQLiteDatabase db = appLockHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("delete from booklock where packname=?",
                    new Object[] { packname });
            db.close();
        }
    }

    public void update(String olderNumber, String newNumber) {
        SQLiteDatabase db = appLockHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("update blacknumber set number=? where number=?",
                    new Object[] { newNumber, olderNumber });
            db.close();
        }
    }

}


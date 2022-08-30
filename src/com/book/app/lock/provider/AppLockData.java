package com.book.app.lock.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppLockData {
    private AppLockDBHelper dbHelper;
    private static final String TABLE = "applock";
    private static final String COLUMN_KEY = "appkey";
    private static final String COLUMN_APPID = "packname";
    private static final String COLUMN_VALUE = "value";
    private static final String[] COLUMNS_FOR_QUERY = { COLUMN_VALUE };

    public AppLockData(Context context) {
        dbHelper = new AppLockDBHelper(context);
    }

    public List<String> getPackName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> packnames = new ArrayList<String>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select packname from applock ", null);
            while (cursor.moveToNext()) {
                String packname = cursor.getString(0);
                packnames.add(packname);
            }
            cursor.close();
            db.close();
        }
        return packnames;
    }


    public boolean find(String packName) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery(
                    "select packname from applock where packname=?",
                    new String[] { packName });
            if (cursor.moveToNext()) {
                result = true;
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    public void add(String key, String packname, String value) {
        if (find(packname)) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put("appkey", key);
        cv.put("packName", packname);
        cv.put("value", value);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            // db.execSQL("insert into applock (packname) values (?)",
            // new Object[] { packname });
            db.insert("applock", null, cv);
            db.close();
        }
    }

    public void delete(String packname) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("delete from applock where packname=?",
                    new Object[] { packname });
            db.close();
        }
    }

    public void update(String olderNumber, String newNumber) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("update blacknumber set number=? where number=?",
                    new Object[] { newNumber, olderNumber });
            db.close();
        }
    }

    public void writeToDb(String key, String value, String appId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_KEY, key);
        cv.put(COLUMN_APPID, appId);
        cv.put(COLUMN_VALUE, value);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE, COLUMN_KEY + "=? AND " + COLUMN_APPID + "=?",
                    new String[] { key, appId });
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public String readFromDb(String key, String defaultValue, String appId) {
        Cursor cursor;
        String result = defaultValue;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if ((cursor = db.query(TABLE, COLUMNS_FOR_QUERY, COLUMN_APPID
                + "=? AND " + COLUMN_KEY + "=?", new String[] { appId, key },
                null, null, null)) != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            cursor.close();
            db.close();
        }
        return result;
    }

}

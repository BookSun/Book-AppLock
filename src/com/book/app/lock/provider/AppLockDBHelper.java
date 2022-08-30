package com.book.app.lock.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppLockDBHelper extends SQLiteOpenHelper {
    private static final String TABLE = "applock";
    private static final String COLUMN_KEY = "appkey";
    private static final String COLUMN_APPID = "packname";
    private static final String COLUMN_VALUE = "value";
    private static final String LOCK_TABLE = "booklock";
    private static final int APPLOCK_VERSION = 2;

    public AppLockDBHelper(Context context) {
        super(context, "applock.db", null, APPLOCK_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_KEY
                + " TEXT," + COLUMN_APPID + " TEXT," + COLUMN_VALUE + " TEXT"
                + ");");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + LOCK_TABLE + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_APPID
                + " TEXT," + COLUMN_VALUE + " TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + LOCK_TABLE + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_APPID
                    + " TEXT," + COLUMN_VALUE + " TEXT" + ");");
        }
    }

}

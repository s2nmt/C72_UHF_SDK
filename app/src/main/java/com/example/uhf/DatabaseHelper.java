package com.example.uhf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "settings.db";
    public static final String TABLE_NAME = "connection_settings";
    public static final String COL_ID = "id";
    public static final String COL_IP = "ip";
    public static final String COL_PORT = "port";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_NAME_SERVER = "name_server";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2); // Increment the version number
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_IP + " TEXT, " +
                COL_PORT + " TEXT, " +
                COL_USERNAME + " TEXT, " +
                COL_PASSWORD + " TEXT, " +
                COL_NAME_SERVER + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertData(String ip, String port, String username, String password, String nameServer) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteOldRecords(db);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_IP, ip);
        contentValues.put(COL_PORT, port);
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_NAME_SERVER, nameServer); // Add the Name_sever value
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result;
    }
    private void deleteOldRecords(SQLiteDatabase db) {
        // Keep only the latest record (delete all others)
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL_ID + " NOT IN " +
                "(SELECT " + COL_ID + " FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC LIMIT 1)");
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}

package com.example.uhf;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectHelper {

    Connection con;
    String uname, pass ,ip ,port, database="iot_yazaki";
    @SuppressLint("NewApi")
    public Connection connectionclass(Context context){
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM connection_settings ORDER BY id DESC LIMIT 1", null);

        if (cursor.moveToFirst()) {
            ip = cursor.getString(cursor.getColumnIndex("ip"));
            uname = cursor.getString(cursor.getColumnIndex("username"));
            pass = cursor.getString(cursor.getColumnIndex("password"));
            port = cursor.getString(cursor.getColumnIndex("port"));
        }
        Log.d("Ip",ip);
        Log.d("pass",pass);
        Log.d("uname",uname);
        Log.d("database",database);
        Log.d("port",port);
        cursor.close();
        dbHelper.close();

        // Now, use the retrieved values to establish a connection
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";" + "databasename=" + database + ";user=" + uname + ";password=" + pass + ";";
            connection = DriverManager.getConnection(ConnectionURL);
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
        return connection;
    }
}

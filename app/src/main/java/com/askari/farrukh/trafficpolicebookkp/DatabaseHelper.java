package com.askari.farrukh.trafficpolicebookkp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by FARRU on 12/8/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    //public static final String DataBaseName = "Test.db";
    public DatabaseHelper(Context context) {
        super(context, "traffic_ts.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `challan` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `challan_no` TEXT, `vehicle_type` TEXT,  `vehicle_no` TEXT, `district` TEXT, `location` TEXT, `education` TEXT, `image` TEXT, `date` TEXT, `lat` TEXT,  `lng` TEXT,  `book_no` TEXT, `details` TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS 'challan'");
        db.execSQL("DROP TABLE IF EXISTS 'road_accident'");
        db.execSQL("DROP TABLE IF EXISTS 'road_infrastructure'");
        onCreate(db);
    }

    public boolean insertDataChallan(String challan_no, String vehicle_type, String vehicle_no, String district, String location, String education, String image, String date, String lat, String lng, String book_no, String details){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("challan_no", challan_no);
        contentValues.put("vehicle_type", vehicle_type);
        contentValues.put("vehicle_no", vehicle_no);
        contentValues.put("district", district);
        contentValues.put("book_no", book_no);
        contentValues.put("lat", lat);
        contentValues.put("lng", lng);
        contentValues.put("image", image);
        contentValues.put("details", details);
        contentValues.put("date", date);
        contentValues.put("location", location);
        contentValues.put("education", education);
        long result = sqLiteDatabase.insert("challan", null, contentValues);
        if (result == -1){
            return false;
        } else return true;
    }

    public Cursor getAllDataChallan(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM challan", null);
        return cursor;
    }
}

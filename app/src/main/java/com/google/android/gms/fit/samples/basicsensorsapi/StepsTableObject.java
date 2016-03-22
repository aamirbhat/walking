package com.google.android.gms.fit.samples.basicsensorsapi;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StepsTableObject {
    SharedPreferences sharedPreferences = null;
    Context context;

    private static final String TAG = "WalkingModule";
    DatabaseHandler db;
    static final String TABLE_STEPS = "counts_table";

    // table_steps Table Columns names
    static final String KEY_ID = "id";
    static final String KEY_SENSOR_DATA = "sensor_data";
    public static final String KEY_REL_DATA = "relative_data";
    public static final String KEY_IS_SYNC = "is_sync";
    static final String KEY_TIMESTAMP = "timestamp";
    final static String[] columns = { KEY_ID, KEY_SENSOR_DATA,KEY_REL_DATA,KEY_TIMESTAMP,KEY_IS_SYNC };
    final static String CREATE_STEPS_TABLE = "CREATE TABLE " + TABLE_STEPS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_SENSOR_DATA + " INTEGER,"
            + KEY_REL_DATA + " INTEGER, " +KEY_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP  " + ")";


    String day = null;
    // constructor
    public StepsTableObject(DatabaseHandler db){
        this.db = db;
    }

    void insert(double sensor_data, double relative_data){
        ContentValues values = new ContentValues();
        values.put(KEY_SENSOR_DATA, sensor_data);
        values.put(KEY_REL_DATA, relative_data);
        this.db.getWritableDatabase().insert(TABLE_STEPS, null, values);
        this.db.close();
    }

    public HashMap<String, String> filter(String value) {
         String selectQuery = "SELECT  sum(" + KEY_REL_DATA + "), strftime('%d', "+ KEY_TIMESTAMP +")" +
                 " as day,strftime('%W', "+ KEY_TIMESTAMP +") as week, strftime('%m', "+ KEY_TIMESTAMP +")" +
                 " as month FROM " + TABLE_STEPS;

         switch (value) {
             case "All Time":
                 selectQuery = selectQuery + "  order by id desc ";
                 break;
             case "This Week":
                 selectQuery = selectQuery + " group by week,month order by id desc ";
                 break;
             case "This Month":
                 selectQuery = selectQuery + " group by month order by id desc limit 1";
                 break;
             default:
                 selectQuery = selectQuery + " group by day,month order by id desc limit 1";;
                 break;
         }

         HashMap<String, String> hm = new HashMap<>();
         Cursor cursor=this.db.getWritableDatabase().rawQuery(selectQuery,null);
         if (cursor.moveToFirst()) {
             Log.i("Bermuda", java.lang.String.valueOf(cursor.getString(0)) + "aamir222");
             hm.put(KEY_REL_DATA, cursor.getString(0));
         }
         this.db.close();
         return hm;

     }

    public String get_not_sync_count(){
        String value = null;
        String selectQuery = "SELECT  sum(" + KEY_REL_DATA + ")  FROM " + TABLE_STEPS + " where "+KEY_IS_SYNC+"=0";
        Cursor cursor=this.db.getWritableDatabase().rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            Log.i("Bermuda", java.lang.String.valueOf(cursor.getString(0)) + "aamaggggggggreir222");
            value = cursor.getString(0);
        }
        this.db.close();
        return value;
    }

    List<HashMap<String, String>> filter(){
        Log.i(TAG, "cursor");
        String selectQuery  = "SELECT * from "+ TABLE_STEPS + " where "+KEY_IS_SYNC+"=0";
         //String day, String month
        Cursor cursor=this.db.getWritableDatabase().rawQuery(selectQuery,null);
        Log.i(TAG, String.valueOf(cursor));
        final List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put(KEY_ID, cursor.getString(0));
                hm.put(KEY_SENSOR_DATA, cursor.getString(1));
                hm.put(KEY_REL_DATA, cursor.getString(2));
                hm.put(KEY_TIMESTAMP, cursor.getString(3));
                aList.add(hm);
            }
            while (cursor.moveToNext());
        }
        this.db.close();
        return aList;
    }

    public void update_sync(List<String> ids){
        String id_values = "";
        for (String id  : ids) {

            id_values=id_values+id+',';
        }
        if (id_values.endsWith(",")) {
            id_values = id_values.substring(0, id_values.length() - 1);

        }
        String query = "UPDATE "+TABLE_STEPS+" set "+ KEY_IS_SYNC +"=1 where "+KEY_ID+" IN ( "+id_values+" )";
        Log.i(TAG, query + "SSSSSSSSSSSSSS");
        Cursor cursor = this.db.getWritableDatabase().rawQuery(query,null);
        if (cursor.moveToFirst()) {
            Log.i("Bermuda", java.lang.String.valueOf(cursor.getString(0)) + "   UPDATE \"+TABLE_STEPS+\" set \"+ KEY_IS_SYNC +\"=1");

        }
        this.db.close();


    }

    String get_last_id(){
         String id = null;
         String selectQuery = "SELECT id from "+ TABLE_STEPS +" order by id DESC limit 1";
         Cursor cursor=this.db.getWritableDatabase().rawQuery(selectQuery,null);
         if (cursor.moveToFirst()) {
             Log.i("WalkingModule", String.valueOf(cursor.getString(0)) + " steps id list");
             id = String.valueOf(cursor.getString(0));
             }
         this.db.close();
         return id;

         }

    HashMap<String, String> get(String id){
        HashMap<String, String> hm = new HashMap<>();
        java.lang.String selectQuery = "SELECT * from "+ TABLE_STEPS +" where id="+id;
        Cursor cursor=this.db.getWritableDatabase().rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            Log.i("WalkingModule", java.lang.String.valueOf(cursor.getString(0)) + "LAst ID count data");
            hm.put(KEY_ID, cursor.getString(0));
            hm.put(KEY_SENSOR_DATA, cursor.getString(1));
            hm.put(KEY_REL_DATA, cursor.getString(2));
            hm.put(KEY_TIMESTAMP, cursor.getString(3));
        }
        this.db.close();
        return hm;
    }

    public ArrayList<HashMap<String, String>> getAllItems() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * from "+TABLE_STEPS;
        Cursor cursor=this.db.getWritableDatabase().rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(KEY_ID, cursor.getString(0));
                map.put(KEY_SENSOR_DATA, cursor.getString(1));
                map.put(KEY_REL_DATA, cursor.getString(2));
                map.put(KEY_TIMESTAMP, cursor.getString(3));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        return wordList;
    }

}







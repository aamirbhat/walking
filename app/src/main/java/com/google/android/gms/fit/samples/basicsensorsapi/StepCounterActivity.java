package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.MultiTapKeyListener;
import android.util.Log;
import android.util.MutableLong;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class StepCounterActivity extends Activity implements SensorEventListener {

    private TextView count_lastEntry;
    private TextView count_reboot;
    private TextView count;
    SharedPreferences sharedPreferences;
    private SensorManager sensorManager;
    StepsTableObject step_obj;
    ListView listView;
    static final String KEY_ID = "id";
    static final String KEY_SENSOR_DATA = "sensor_data";
    public static final String KEY_REL_DATA = "relative_data";
    static final String KEY_TIMESTAMP = "timestamp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        sharedPreferences  = getSharedPreferences("MyPreferences" , MODE_MULTI_PROCESS);
        count_lastEntry = (TextView) findViewById(R.id.count_lastEntry);
        count_reboot = (TextView) findViewById(R.id.count_reboot);
        count = (TextView) findViewById(R.id.count);
        listView = (ListView) findViewById(R.id.list_item);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        DatabaseHandler db = new DatabaseHandler(this);
        StepsTableObject step_obj = new StepsTableObject(db);
        this.step_obj = step_obj;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            Toast.makeText(this, "Step count sensor is available....!", Toast.LENGTH_LONG).show();
            sensorManager.registerListener(this, countSensor,
                    SensorManager.SENSOR_DELAY_UI);
            String server_set = sharedPreferences.getString("is_server_sync", "False");
            if (server_set.equals("False")){
                setServerSync();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("is_server_sync", "true");
                editor.commit();
            }
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("Walking_Module"));
        ArrayList<HashMap<String, String>> itemList =  step_obj.getAllItems();
        if (itemList.size() != 0){
            ListAdapter adapter = new SimpleAdapter( StepCounterActivity.this,itemList, R.layout.listview_customrow,
                    new String[] { KEY_ID,KEY_REL_DATA, KEY_SENSOR_DATA, KEY_TIMESTAMP}, new int[] {R.id.Rank_Cell, R.id.Relative_Count, R.id.Sensor_Count, R.id.TimeStamp});
            listView.setAdapter(adapter);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String relativeCount = intent.getStringExtra("RelativeCount");
            String sensorCount = intent.getStringExtra("SensorCount");
            count_lastEntry.setText(relativeCount);
            count_reboot.setText(sensorCount);
            Log.d("receiver", "Got relative Count: " + relativeCount);

        }
    };

    public void setServerSync(){

        Intent intent = new Intent(this, BackgroundStep.class);
// Create the pending intent and wrap our intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
// Get the alarm manager service and schedule it to go off after 3s
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC, 6000, 90000, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC, 5000, 300000, pendingIntent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        count.setText(String.valueOf(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}

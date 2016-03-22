package com.google.android.gms.fit.samples.basicsensorsapi;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;

public class StepCountService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor stepcounter;
    private String TAG = "Bermuda";
    private StepsTableObject step_obj;
    int counter=0;
    SharedPreferences sharedPreferences;

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get a reference to the stepcounter
        stepcounter = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // Register for sensor updates

        mSensorManager.registerListener(this, stepcounter,
                SensorManager.SENSOR_DELAY_NORMAL);
        DatabaseHandler db = new DatabaseHandler(this);

        StepsTableObject step_obj = new StepsTableObject(db);
        this.step_obj = step_obj;
        this.counter =0;
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sharedPreferences = this.getSharedPreferences("MyPreferences", MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IsSensorActive", true);
        editor.commit();
        String last_id = step_obj.get_last_id();
        double relative;
        Log.i(TAG, "Walking Last ID " + last_id);
        if (last_id == null){
            relative = 0.0;
        }
        else{
            HashMap<String, String> data = step_obj.get(last_id);
            Log.i(TAG, "Walking Data " + data);
            String sensor_data = data.get(StepsTableObject.KEY_SENSOR_DATA);
            relative = (int) event.values[0] - Integer.parseInt(sensor_data);
            if (relative < 0){
                relative = (int) event.values[0];
            }
        }
        step_obj.insert(event.values[0], relative);
        Intent intent = new Intent("Walking_Module");
        intent.putExtra("SensorCount",String.valueOf(event.values[0]));
        intent.putExtra("RelativeCount", String.valueOf(relative));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i(TAG, "Aw event value........" + String.valueOf(event.values[0]));
        Log.i(TAG, "Aw relative........" + String.valueOf(relative));
        mSensorManager.unregisterListener(this);
        stopSelf();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

package com.google.android.gms.fit.samples.basicsensorsapi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BackgroundStep extends BroadcastReceiver {
    private String TAG = "Bermuda";


    @Override
    public void onReceive(final Context arg0, Intent arg1) {
        // For our recurring task, we'll just display a message
        Log.i("Bermuda: ", "background hahah ..");
//        Toast.makeText(arg0, "I'm running", Toast.LENGTH_SHORT).show();
        final Intent gpsIntent = new Intent(arg0, StepCountService.class);
        gpsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        arg0.getApplicationContext().startService(gpsIntent);

    }
}

package com.google.android.gms.fit.samples.basicsensorsapi;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import database.DatabaseHandler;
//import database.StepTable;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

//import com.google.android.gms.fit.samples.common.logger.Log;
//import com.google.android.gms.fit.samples.common.logger.LogView;
//import com.google.android.gms.fit.samples.common.logger.LogWrapper;
//import com.google.android.gms.fit.samples.common.logger.MessageOnlyLogFilter;


public class FitService extends Service implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public GoogleApiClient mGoogleApiClient = null;
    Context context;
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    int steps;
    String starts_ts;
    String end_ts;
    int stepcount = 0;
    private OnDataPointListener mListener;
    // [END mListener_variable_reference]

    public FitService() {
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .build();
    }
    @Override
    public void onCreate() {
        Log.i("Fit------------", "******************");
        context = this.getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buildGoogleApiClient();
        mGoogleApiClient.connect(1);
        Log.i("Fit-----------", "****************** mGoogleApiClient.connect");
//        return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("Fit-------------", "**************** Connected" + "0900909 Fitness connected");
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if( DataType.TYPE_STEP_COUNT_DELTA.equals( dataSource.getDataType() ) ) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_DELTA);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mGoogleApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
        new GetSteps().execute();
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    stepcount = stepcount + Integer.parseInt(String.valueOf(val));
                    Toast.makeText(context, "StepCount****"+stepcount, Toast.LENGTH_SHORT).show();

                }
            }
        };

        SensorRequest request = new SensorRequest.Builder()
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate(10, TimeUnit.SECONDS )
                .build();
        Fitness.SensorsApi.add( mGoogleApiClient, request, mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e("GoogleFit", "SensorApi successfully added");
                        }
                    }
                });
    }

    public class GetSteps extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params) {
            int steps = getStepsToday();
            return String.valueOf(steps);
        }

        @Override
        protected void onPostExecute(String text) {
            if (text != null)
                publishTodaysStepData(Integer.parseInt(text));
        }
    }
    private int getStepsToday() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        final DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(30, TimeUnit.SECONDS);

        DataSet stepData = dataReadResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);

        int totalSteps = 0;

        for (DataPoint dp : stepData.getDataPoints()) {
            for(Field field : dp.getDataType().getFields()) {
                int steps = dp.getValue(field).asInt();
                totalSteps += steps;
            }
        }
        return totalSteps;
    }

    private void publishTodaysStepData(int totalSteps) {
        //stepCountView.setText(String.valueOf(totalSteps));
        Intent intt = new Intent("BROADCAST_ACTION");
        intt.putExtra("message", String.valueOf(totalSteps));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intt);
        Toast.makeText(this,"---------------fit  today steps"+String.valueOf(totalSteps), Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("FitHistory", "*******************Connection suspended");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("FitHistory", "****************** Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.toString() + +connectionResult.getErrorCode());

    }
}

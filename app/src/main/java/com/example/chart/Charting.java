package com.example.chart;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

import java.util.ArrayList;


public class Charting extends Activity {

    private LineChart mChart;
    private List<Entry> mEntries;
    private int mIndex = 0;

    private static final String TAG = "BlueTest5-MainActivity";
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;
    private ArrayList<Integer> values = new ArrayList<>();




    private boolean mIsUserInitiatedDisconnect = false;


    private CheckBox chkReceiveText;
    private  CheckBox  chkFile;
    private Button disc;
    private Button reset;


    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    //file system

    String FILENAME =  DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
    File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File myFile = new File(folder,"Stress"+ FILENAME+".txt"); ;
    FileOutputStream fstream;

    //file system

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charting);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
        Log.d(TAG, "Ready");
        disc=findViewById(R.id.disc);
        chkReceiveText=findViewById(R.id.chkReceiveText);
        chkFile=findViewById(R.id.chkFile);
        reset=findViewById(R.id.reset);


        disc.setOnClickListener(view -> {

            if (mBTSocket!=null)
            {
                try
                {
                    Log.d(TAG, "Array Size: " + values.size() );
                    mIsBluetoothConnected=false;
                    mBTSocket.close();
                    Intent mainIntent=new Intent(Charting.this,Results.class);
                    mainIntent.putIntegerArrayListExtra("myArray", (ArrayList<Integer>) values);
                    startActivity(mainIntent);
                }
                catch (IOException e)
                { }
            }
            finish();
        });

        reset.setOnClickListener(v -> {
            restartRecalibrate();
            Intent intent1 =getIntent();
            finish();
            startActivity(intent1);
        });


        //for charting purpose

        mChart = findViewById(R.id.chart1);
        mEntries = new ArrayList<>();

        // Disable description text
        mChart.getDescription().setEnabled(false);


        // Enable touch gestures
        mChart.setTouchEnabled(true);

        // Enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // If disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // Set the background color
      //  mChart.setBackgroundColor(Color.WHITE);
        mChart.setBackgroundColor(Color.parseColor("#F6FCFB"));


        // Add an empty data set to the chart
        LineData data = new LineData();
        mChart.setData(data);

        // Customize x-axis
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setGranularity(2f); // only intervals of 2 unit
        xAxis.setLabelCount(5); // show 5 labels
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Convert float to int to display as label
                return String.valueOf((int) value);
            }
        });

        // Customize y-axis
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(120f);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD);


        //removable for right axis
        YAxis rightAxis=mChart.getAxisRight();
        rightAxis.setEnabled(false);

        //removable for right axis

        // Add empty data to the chart
        addEntry(0);
        //for charting purpose


        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //file system

    }



    private class ReadInput implements Runnable {


        private boolean bStop = false;
        private Thread t;




        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();

                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        int gsrV=extractNumberFromString(strInput);
                        String str = Integer.toString(gsrV);

                        //store in array
                        if(gsrV!=0){
                            values.add(gsrV);
                        }
                        //store in array

                        //file system

                        if(chkFile.isChecked() && gsrV!=0) {
                            try {
                                fstream = new FileOutputStream(myFile, true);
                                fstream.write(str.getBytes());
                                fstream.write(" , ".getBytes());
                                fstream.close();

                            } catch (IOException e) {
                                Log.e(TAG, "Error in writing file");
                            }
                        }

                        //file system

                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */

                        if (chkReceiveText.isChecked()) {
                            //  mTxtReceive.post(() -> {
                            //  mTxtReceive.append(strInput);
                            //  });
                            if(gsrV!=0) {
                                addEntry(gsrV);
                            }

                        }

                    }
                    Thread.sleep(2000);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        private int extractNumberFromString(String strInput) {
            int number=0;
            boolean found=false;
            for(int i=0;i<strInput.length();i++)
            {
                if(Character.isDigit(strInput.charAt(i))) {
                    found = true;
                    number = number * 10 + (strInput.charAt(i) - '0');
                }
                else if(found)
                {
                    break;
                }
            }

            return number;
        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning()) ; // Wait until it stops
                mReadThread = null;
            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(Charting.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

     /*   */

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    if (ContextCompat.checkSelfPermission(Charting.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(Charting.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(Charting.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
                        }
                    } else {
                        // Check if the Bluetooth socket and device information exists in onRetainNonConfigurationInstance()
                        Charting.BluetoothSocketWrapper socketWrapper = (Charting.BluetoothSocketWrapper) getLastNonConfigurationInstance();
                        if (socketWrapper != null) {
                            mBTSocket = socketWrapper.socket;
                            mDevice = socketWrapper.device;
                        } else {
                            mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                            mBTSocket.connect();
                        }

                        if (mBTSocket.isConnected()) {
                            Log.d(TAG, "mBTIsConnected ");
                        } else {
                            Log.d(TAG, "mBTNotConnected ");
                        }
                    }
                }
            } catch (IOException e) {
                // Unable to connect to device
                Log.e(TAG, "Error occurred while trying to connect to device: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Could not connect to device: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                mConnectSuccessful = false;
            } catch (SecurityException e) {
                // Permission denied
                Log.e(TAG, "Permission denied " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Permission denied " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();

        }

    }

    private ILineDataSet createSet() {
        LineDataSet set = new LineDataSet(mEntries, "Stress Score");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.parseColor("#229D9B"));
        //set.setColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleSize(4f);
        set.setDrawCircles(true);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    private void addEntry(int value) {
        // Add new data to the chart
        //   mEntries.add(new Entry(mIndex, value));
        //   mIndex++;

        //removable

        if(mEntries.size()>0 && mEntries.get(mEntries.size()-1).getX()==mIndex){
            mEntries.set(mEntries.size()-1,new Entry(mIndex,value));
        }
        else
        {
            mEntries.add(new Entry(mIndex,value));
        }

        mIndex++;
        //removable

        // Update the data set
        LineData data = mChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set =createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(mIndex, value), 0);
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(10); // display only the last 10 entries
            // Move the chart to the latest entry
            mChart.moveViewToX(mEntries.size() - 1);

        }



    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return new Charting.BluetoothSocketWrapper(mBTSocket, mDevice);
    }

    private static class BluetoothSocketWrapper {
        public BluetoothSocket socket;
        public BluetoothDevice device;

        public BluetoothSocketWrapper(BluetoothSocket socket, BluetoothDevice device) {
            this.socket = socket;
            this.device = device;
        }
    }

    private void restartRecalibrate() {
        OutputStream outputStream;
        String message = "R";

        try {
            outputStream = mBTSocket.getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();
        }catch (IOException e){
            Log.e(TAG, "Error sending message to Arduino", e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}
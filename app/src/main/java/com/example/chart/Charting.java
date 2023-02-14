package com.example.chart;


import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.achartengine.GraphicalView;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Charting extends Activity {

    private GraphicalView graphView;
    private LineGraph lineGraph;
    private LinearLayout chart;
    int times=0;

    private static final String TAG = "BlueTest5-MainActivity";
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;


    private boolean mIsUserInitiatedDisconnect = false;

    // All controls here
    private Button mBtnClearInput;
    private CheckBox chkReceiveText;


    private Button disc;


    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charting);
        ActivityHelper.initialize(this);

        lineGraph = new LineGraph();
        chart=findViewById(R.id.chart);
        graphView = lineGraph.getView(this);
        chart.addView(graphView);
        chkReceiveText = findViewById(R.id.chkReceiveText);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        Log.d(TAG, "Ready");
        mBtnClearInput = findViewById(R.id.Clear);
        disc=findViewById(R.id.disc);

        mBtnClearInput.setOnClickListener(stopInput());

        disc.setOnClickListener(view -> {

            if (mBTSocket!=null)
            {
                try
                {
                    mIsBluetoothConnected=false;
                    mBTSocket.close();
                    Intent mainIntent=new Intent(Charting.this,MainActivity.class);
                    startActivity(mainIntent);
                }
                catch (IOException e)
                { }
            }
            finish();
        });


    }

    private View.OnClickListener stopInput() {
        times=0;
        graphView.repaint();
        return null;
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
                        times+=1;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        int gsrV=extractNumberFromString(strInput);
                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */

                        if (chkReceiveText.isChecked()) {
                        //    mTxtReceive.post(() -> {
                             //   mTxtReceive.append(strInput);

                                lineGraph.addNewPoints(Point.Pointset(times,gsrV));
                                graphView.repaint();

                        //    });
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
                while (mReadThread.isRunning())
                    ; // Wait until it stops
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

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {

                    if (ContextCompat.checkSelfPermission(Charting.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(Charting.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(Charting.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                            ActivityCompat.requestPermissions(Charting.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);

                        }
                    }

                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);

                    if (ContextCompat.checkSelfPermission(Charting.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(Charting.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                        }

                    }

                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                    mBTSocket.connect();
                }
            } catch (IOException e) {
                // Unable to connect to device
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



}
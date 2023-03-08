package com.example.chart;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MonitoringScreen extends Activity {

    private static final String TAG = "BlueTest5-MainActivity";
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;


    private boolean mIsUserInitiatedDisconnect = false;

    // All controls here
    private TextView mTxtReceive;
    private Button mBtnClearInput;
    private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    private Button disc;


    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_screen);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
        Log.e(TAG, "Ready");
        mTxtReceive = findViewById(R.id.txtReceive);
        chkScroll = findViewById(R.id.chkScroll);
        chkReceiveText = findViewById(R.id.chkReceiveText);
        scrollView = findViewById(R.id.viewScroll);
        mBtnClearInput = findViewById(R.id.btnClearInput);
        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
        disc=findViewById(R.id.disc);

        mBtnClearInput.setOnClickListener(arg0 -> mTxtReceive.setText(""));

        disc.setOnClickListener(view -> {

            if (mBTSocket!=null)
            {
                try
                {
                    mIsBluetoothConnected=false;
                    mBTSocket.close();
                    Intent mainIntent=new Intent(MonitoringScreen.this,MainActivity.class);
                    startActivity(mainIntent);
                }
                catch (IOException e)
                { }
            }
            finish();
        });


    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;
        InputStream inputStream;

        public ReadInput() {
            InputStream input = null;
            t = new Thread(this, "Input Thread");
            t.start();

            try {
                input = mBTSocket.getInputStream();
            } catch (IOException e) { }

            inputStream = input;
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        public void run() {
          //  InputStream inputStream;

            try {
                    while (!bStop) {
                        byte[] buffer = new byte[256];
                        int bytesRead = 0;
                        Log.d(TAG, "Inside while Input Stream:    " + inputStream);

                        try {
                                synchronized (this) {
                                bytesRead = inputStream.read(buffer);
                                Log.d(TAG, "Inside data: ");
                                }

                        //    if (bytesRead > 0) {
                                String strInput = new String(buffer, 0, bytesRead);


                                if (chkReceiveText.isChecked()) {
                                    mTxtReceive.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTxtReceive.append(strInput);

                                            int txtLength = mTxtReceive.getEditableText().length();
                                            if (txtLength > mMaxChars) {
                                                mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                                            }

                                            if (chkScroll.isChecked()) {
                                                scrollView.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        scrollView.fullScroll(View.FOCUS_DOWN);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                         //   }
                        } catch (IOException e) {
                            Log.e(TAG, "Error occurred while reading data from input stream: " + e.getMessage());
                            e.printStackTrace();
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "Unexpected error occurred: " + e.getMessage());
                            e.printStackTrace();
                            break;
                        }
                    }

            } catch (Exception e) {
                Log.e(TAG, "Unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    mBTSocket.close();
                    Log.d(TAG, "Socket Closed ");
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred while closing socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }



      /*  public void run() {

            InputStream inputStream;
            try {
                inputStream = mBTSocket.getInputStream();


                Log.d(TAG, "Ready to receive data");

                while (!Thread.currentThread().isInterrupted()) {

                    byte[] buffer = new byte[1024];

                    Log.d(TAG, "Entered byte"+inputStream);
                      if(inputStream.available() > 0){

                        Log.d(TAG, "Entered Luckily");


                        int bytesRead = inputStream.read(buffer);

                        if (bytesRead > 0) {
                            final String strInput = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);


                            if (chkReceiveText.isChecked()) {

                                mTxtReceive.post(() -> {
                                    runOnUiThread(() -> {
                                        mTxtReceive.append(strInput);

                                        int txtLength = mTxtReceive.getEditableText().length();
                                        if (txtLength > mMaxChars) {
                                            mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                                        }

                                        if (chkScroll.isChecked()) {
                                            scrollView.fullScroll(View.FOCUS_DOWN);
                                        }
                                    });
                                });

                            }
                        }
                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while reading data from input stream: " + e.getMessage());
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread was interrupted: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error occurred: " + e.getMessage());
            }finally {
                try {
                    mBTSocket.close();
                    Log.d(TAG, "Socket Closed ");
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred while closing socket: " + e.getMessage());
                }
            }
        }  */

        public void stop() {
            bStop = true;
        }

    }

   /* private class DisConnectBT extends AsyncTask<Void, Void, Void> {

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

    } */

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning()) {
                    try {
                        Thread.sleep(100); // Wait until it stops
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mReadThread = null;
            }

            try {
                if (mBTSocket != null) {
                    mBTSocket.close();
                }
            } catch (IOException e) {
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

 /*   @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    } */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mBTSocket != null && mIsBluetoothConnected) {
                new DisConnectBT().execute();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while pausing activity: " + e.getMessage());
        }
        Log.d(TAG, "Paused");
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

   // @SuppressLint("StaticFieldLeak")
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MonitoringScreen.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

     /*   @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {

                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(MonitoringScreen.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);

                        }
                    }

                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);

                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
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
        }  */

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
                        }
                    } else {

                       mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        mBTSocket.connect();

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
            /*    mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader//
                mReadThread.start(); // Start the read thread  */

                ReadInput readInput = new ReadInput();
                Thread inputThread = new Thread(readInput);
                inputThread.start();


            }


            if (progressDialog != null && progressDialog.isShowing() && !isFinishing()) {
                runOnUiThread(() -> progressDialog.dismiss());
            } else {
                Log.e(TAG, "Progress dialog is null or not showing or activity is finishing");
            }


        }



    /*    @Override
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

           if(progressDialog!=null && progressDialog.isShowing()) {
               progressDialog.dismiss();
           }
        }  */

    }



}
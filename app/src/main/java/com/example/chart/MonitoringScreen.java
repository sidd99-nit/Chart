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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MonitoringScreen.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }




        @Override
       protected Void doInBackground(Void... devices) {
           try {
               if (mBTSocket == null || !mIsBluetoothConnected) {
                   if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                           ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 2);
                       }
                   } else {
                       // Check if the Bluetooth socket and device information exists in onRetainNonConfigurationInstance()
                       BluetoothSocketWrapper socketWrapper = (BluetoothSocketWrapper) getLastNonConfigurationInstance();
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



    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return new BluetoothSocketWrapper(mBTSocket, mDevice);
    }

    private static class BluetoothSocketWrapper {
        public BluetoothSocket socket;
        public BluetoothDevice device;

        public BluetoothSocketWrapper(BluetoothSocket socket, BluetoothDevice device) {
            this.socket = socket;
            this.device = device;
        }
    }

}
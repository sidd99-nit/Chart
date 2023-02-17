package com.example.chart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LoadingActivity extends AppCompatActivity {

     static final long LOADING_DELAY=4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        },LOADING_DELAY);
    }

    private void startMainActivity() {
        Intent intent=new Intent(LoadingActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }


}
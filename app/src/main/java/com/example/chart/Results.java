package com.example.chart;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;



import java.io.BufferedReader;
import java.io.FileInputStream;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import java.util.ArrayList;

public class Results extends AppCompatActivity {

    private LineChart mChart;
    private List<Entry> mEntries;
    private  Integer[] array;
    private Button back;
    private int min=100;
    private int max=0;
    private float avg=0;
    private int sum=0;
    private TextView stats;
    private String fileNameString;

    //added new functionality of past records

    private Button add;

    private String dataOverDb;
    private String maxString;
    private String avgString;
    private String minSting;
    private DBHandler dbHandler;
    private String fileName;

    //added new functionality of past records


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        dbHandler = new DBHandler(Results.this);


        back = findViewById(R.id.back);
        stats = findViewById(R.id.stats);

        //added new functionality of past records
         add = findViewById(R.id.addData);
        //added new functionality of past records


        Intent intent = getIntent();
        ArrayList<String> myArray = intent.getStringArrayListExtra("myArray");

        //added new functionality of past records
        fileNameString = intent.getStringExtra("fileName");
        //added new functionality of past records

        array = myArray.toArray(new Integer[myArray.size()]);

        //finding max and min
            for(int i=0;i<array.length;i++){
                if(array[i] > max){
                    max = array[i];
                }
                if(array[i] < min){
                    min = array[i];
                }
                sum+=array[i];
            }
            avg = sum/array.length;

            stats.setText("\t\t\t Max : "+ max + "\t\t\t\t\t\t\t\t\t\t\t Min : "+ min + "\n\n\n \t\t\t Average : "+ avg  );
            stats.setTextSize(18);
            stats.setTypeface(Typeface.DEFAULT_BOLD);

        //finding max and min



        Log.d(TAG, "Array size in results:" + array.length);

        back.setOnClickListener(v -> {
            Intent mainIntent=new Intent(Results.this,MainActivity.class);
            startActivity(mainIntent);
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.addNewCourse(maxString, minSting, avgString, dataOverDb, fileName);
                Toast.makeText(Results.this, "Result has been added.", Toast.LENGTH_SHORT).show();
            }
        });


        mChart = findViewById(R.id.chart1);
        mEntries = new ArrayList<>();

        mChart.getDescription().setEnabled(false);


        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        mChart.setPinchZoom(true);

        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        mChart.setData(data);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setAxisLineWidth(1);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisLineWidth(1);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD);


        YAxis rightAxis=mChart.getAxisRight();
        rightAxis.setEnabled(false);


        for(int i=0;i<array.length;i++)
        {
          mEntries.add(new Entry(i,array[i]));
        }

        //added new functionality of past records

        dataOverDb = Arrays.toString(array);
        maxString = Integer.toString(max);
        minSting = Integer.toString(min);
        avgString = Float.toString(avg);
        fileName = fileNameString;
        //added new functionality of past records


//        mEntries.add(new Entry(0, 50));
//        mEntries.add(new Entry(1, 70));
//        mEntries.add(new Entry(2, 60));
//        mEntries.add(new Entry(3, 80));
//        mEntries.add(new Entry(4, 70));
//        mEntries.add(new Entry(5, 50));
//        mEntries.add(new Entry(6, 70));
//        mEntries.add(new Entry(7, 60));
//        mEntries.add(new Entry(8, 80));
//        mEntries.add(new Entry(9, 70));
//        mEntries.add(new Entry(10, 70));
//        mEntries.add(new Entry(11, 20));
//        mEntries.add(new Entry(12, 50));
//        mEntries.add(new Entry(13, 40));
//        mEntries.add(new Entry(14, 60));
//        mEntries.add(new Entry(15, 10));
//        mEntries.add(new Entry(16, 30));
//        mEntries.add(new Entry(17, 50));
//        mEntries.add(new Entry(18, 70));
//        mEntries.add(new Entry(19, 60));
//        mEntries.add(new Entry(20, 80));
//        mEntries.add(new Entry(21, 70));
//        mEntries.add(new Entry(22, 50));
//        mEntries.add(new Entry(23, 70));
//        mEntries.add(new Entry(24, 60));
//        mEntries.add(new Entry(25, 80));
//        mEntries.add(new Entry(26, 70));
//        mEntries.add(new Entry(27, 70));
//        mEntries.add(new Entry(28, 20));
//        mEntries.add(new Entry(29, 50));
//        mEntries.add(new Entry(30, 40));
//        mEntries.add(new Entry(31, 60));
//        mEntries.add(new Entry(32, 10));
//        mEntries.add(new Entry(33, 30));
//        mEntries.add(new Entry(34, 10));
//        mEntries.add(new Entry(35, 30));
//        mEntries.add(new Entry(36, 50));
//        mEntries.add(new Entry(37, 70));
//        mEntries.add(new Entry(38, 60));
//        mEntries.add(new Entry(39, 80));
//        mEntries.add(new Entry(40, 70));
//        mEntries.add(new Entry(41, 50));
//        mEntries.add(new Entry(42, 70));
//        mEntries.add(new Entry(43, 60));
//        mEntries.add(new Entry(44, 80));
//        mEntries.add(new Entry(45, 70));
//        mEntries.add(new Entry(46, 70));
//        mEntries.add(new Entry(47, 20));
//        mEntries.add(new Entry(48, 50));
//        mEntries.add(new Entry(49, 40));
//        mEntries.add(new Entry(50, 60));
//        mEntries.add(new Entry(51, 10));
//        mEntries.add(new Entry(52, 30));




        LineDataSet dataSet = new LineDataSet(mEntries, "Data Set");
        dataSet.setColor(Color.parseColor("#0086b3"));
        dataSet.setLineWidth(1.5f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);


        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        mChart.setVisibleXRangeMaximum(array.length);

    }


}
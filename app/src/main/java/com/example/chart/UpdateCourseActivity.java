package com.example.chart;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class UpdateCourseActivity extends AppCompatActivity {

    // variables for our edit text, button, strings and dbhandler class.
    private EditText courseNameEdt, courseTracksEdt, courseDurationEdt, courseDescriptionEdt;
    private Button updateCourseBtn;
    private DBHandler dbHandler;
    String maxValue, minValue, avgValue, fileName;
    String dataOverDb;
    private LineChart mChart;
    String[] numberFromString;
    private List<Entry> mEntries;
    int[] dataArray;

    //For Setting The Statistics

    private TextView maxValueTV;
    private TextView minValueTV;
    private TextView avgValueTV;
    private TextView fileNameTV;
    private Button deleteCourseBtn;

    //For Setting The Statistics

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_course);

        // initializing all our variables.
//        courseNameEdt = findViewById(R.id.idEdtCourseName);
//        courseTracksEdt = findViewById(R.id.idEdtCourseTracks);
//        courseDurationEdt = findViewById(R.id.idEdtCourseDuration);
//        courseDescriptionEdt = findViewById(R.id.idEdtCourseDescription);
        updateCourseBtn = findViewById(R.id.idBtnUpdateCourse);
        mChart = findViewById(R.id.chart1);
        mEntries = new ArrayList<>();

        //For Setting The Statistics

        maxValueTV = (TextView)findViewById(R.id.maxValueView);
        minValueTV = (TextView)findViewById(R.id.minValueView);
        avgValueTV = (TextView)findViewById(R.id.avgValueView);
        fileNameTV = (TextView)findViewById(R.id.fileNameView);

        deleteCourseBtn = findViewById(R.id.idBtnDelete);

        //For Setting The Statistics

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
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);
        xAxis.setAxisLineWidth(2);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisLineWidth(2);
        leftAxis.setAxisMaximum(100f);


        YAxis rightAxis=mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // on below line we are initializing our dbhandler class.
        dbHandler = new DBHandler(UpdateCourseActivity.this);

        // on below lines we are getting data which
        // we passed in our adapter class.
        maxValue = getIntent().getStringExtra("max");
        minValue = getIntent().getStringExtra("min");
        avgValue = getIntent().getStringExtra("avg");
        fileName = getIntent().getStringExtra("name");
        dataOverDb = getIntent().getStringExtra("data");

        //For Setting The Statistics

        maxValueTV.setText("Max : "+maxValue);
        minValueTV.setText("Min : "+minValue);
        avgValueTV.setText("Avg : "+avgValue);
        fileNameTV.setText(fileName);
        //For Setting The Statistics



//        numberFromString = dataOverDb.split(",");
        numberFromString = dataOverDb.replaceAll("\\[", "")
                .replaceAll(" ","")
                .replaceAll("]", "")
                .split(",");


        for(int i=0;i<numberFromString.length;i++){
            Log.d(TAG, "i: " + numberFromString[i]);
        }

        dataArray = new int[numberFromString.length];

        for(int i=0;i<numberFromString.length;i++)
        {
            try {
                dataArray[i] = Integer.valueOf(numberFromString[i]);
            }catch (NumberFormatException e){
                Log.e(TAG, "Conversion error from string");
            }
        }

        Log.d(TAG, "DATA ARRAY SIZE: " + dataArray.length);

        for(int i=0;i<dataArray.length;i++){
            mEntries.add(new Entry(i,dataArray[i]));
        }

        LineDataSet dataSet = new LineDataSet(mEntries, "Data Set");
        dataSet.setColor(Color.parseColor("#0086b3"));

        dataSet.setLineWidth(1.5f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawCircles(false);


        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        mChart.setVisibleXRangeMaximum(100); // display only the last 10 entries

        // setting data to edit text
        // of our update activity.
//        courseNameEdt.setText(courseName);
//        courseDescriptionEdt.setText(courseDesc);
//        courseTracksEdt.setText(courseTracks);
//        courseDurationEdt.setText(courseDuration);

        // adding on click listener to our update course button.
        updateCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // inside this method we are calling an update course
                // method and passing all our edit text values.
//                dbHandler.updateCourse(courseName, courseNameEdt.getText().toString(), courseDescriptionEdt.getText().toString(), courseTracksEdt.getText().toString(), courseDurationEdt.getText().toString());

                // displaying a toast message that our course has been updated.
                Toast.makeText(UpdateCourseActivity.this, "Review Done..", Toast.LENGTH_SHORT).show();

                // launching our main activity.
                Intent i = new Intent(UpdateCourseActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        // adding on click listener for delete button to delete our course.
        deleteCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling a method to delete our course.
                dbHandler.deleteCourse(fileName);
                Toast.makeText(UpdateCourseActivity.this, "Record Deleted..", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(UpdateCourseActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
}

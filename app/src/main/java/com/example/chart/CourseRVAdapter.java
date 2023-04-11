package com.example.chart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseRVAdapter extends RecyclerView.Adapter<CourseRVAdapter.ViewHolder> {

    // variable for our array list and context
    private ArrayList<CourseModal> courseModalArrayList;
    private Context context;

    // constructor
    public CourseRVAdapter(ArrayList<CourseModal> courseModalArrayList, Context context) {
        this.courseModalArrayList = courseModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // on below line we are inflating our layout
        // file for our recycler view items.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // on below line we are setting data
        // to our views of recycler view item.
        CourseModal modal = courseModalArrayList.get(position);
        holder.maxValueTV.setText("Max: "+modal.getMaxValue());
        holder.maxValueTV.setTypeface(null,Typeface.ITALIC);

        holder.minValueTV.setText("Min: "+modal.getMinValue());
        holder.minValueTV.setTypeface(null,Typeface.ITALIC);

        holder.avgValueTV.setText("Avg: "+modal.getAvgValue());
        holder.avgValueTV.setTypeface(null,Typeface.ITALIC);

        holder.fileNameTV.setText("Name: "+modal.getFileName());
        holder.fileNameTV.setTypeface(Typeface.DEFAULT_BOLD);
//        holder.dataOverDbValueTV.setText(modal.getDataOverDb());



// below line is to add on click listener for our recycler view item.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // on below line we are calling an intent.
                Intent i = new Intent(context, UpdateCourseActivity.class);

                // below we are passing all our values.
                i.putExtra("max", modal.getMaxValue());
                i.putExtra("min", modal.getMinValue());
                i.putExtra("avg", modal.getAvgValue());
                i.putExtra("data", modal.getDataOverDb());
                i.putExtra("name", modal.getFileName());

                // starting our activity.
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        // returning the size of our array list
        return courseModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // creating variables for our text views.
        private TextView maxValueTV, minValueTV, avgValueTV, dataOverDbValueTV, fileNameTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views
            maxValueTV = itemView.findViewById(R.id.idTVMaxValue);
            minValueTV = itemView.findViewById(R.id.idTVMinValue);
            avgValueTV = itemView.findViewById(R.id.idTVAvgValue);
            fileNameTV = itemView.findViewById(R.id.idTVFileName);
//          dataOverDbValueTV = itemView.findViewById(R.id.idTVDataOverDb);
        }
    }
}

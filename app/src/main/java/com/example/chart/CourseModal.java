package com.example.chart;

public class CourseModal {

    // variables for our coursename,
    // description, tracks and duration, id.
    private String minValue;
    private String maxValue;
    private String avgValue;
    private String dataOverDb;
    private  String fileName;
    private int id;

    // creating getter and setter methods
    public String getMinValue() { return minValue; }

    public void setMinValue(String minValue)
    {
        this.minValue = minValue;
    }

    public String getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(String maxValue)
    {
        this.maxValue = maxValue;
    }

    public  String getFileName(){
        return  fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getAvgValue() { return avgValue; }

    public void setAvgValue(String avgValue)
    {
        this.avgValue = avgValue;
    }

    public String getDataOverDb()
    {
        return dataOverDb;
    }

    public void
    setDataOverDb(String dataOverDb)
    {
        this.dataOverDb = dataOverDb;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    // constructor
    public CourseModal(String maxValue,
                       String minValue,
                       String avgValue,
                       String dataOverDb,
                       String fileName)
    {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.avgValue = avgValue;
        this.dataOverDb = dataOverDb;
        this.fileName = fileName;
    }
}

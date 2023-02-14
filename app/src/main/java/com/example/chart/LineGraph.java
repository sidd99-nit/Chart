package com.example.chart;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class LineGraph {

    private TimeSeries data = new TimeSeries("GSR");
    private XYMultipleSeriesDataset multiData = new XYMultipleSeriesDataset();
    private XYSeriesRenderer renderer = new XYSeriesRenderer();
    private XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
    private GraphicalView view;


    public LineGraph() {
        // add data
        multiData.addSeries(data);

        // customize
        renderer.setColor(Color.BLACK);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setChartValuesSpacing(20);
        renderer.setFillPoints(true);
        renderer.setLineWidth(3);
        renderer.setDisplayChartValues(true);
        renderer.setChartValuesTextSize(30);


        // Enable zoom
        final double min = multiRenderer.getXAxisMin();
        final double max = multiRenderer.getXAxisMax();

        multiRenderer.setBackgroundColor(Color.WHITE);
        multiRenderer.setZoomButtonsVisible(true);
        multiRenderer.setXTitle("Time(s)");
        multiRenderer.setYTitle("GSR");
        multiRenderer.setLabelsColor(Color.RED);
        multiRenderer.setAxisTitleTextSize(20);
        multiRenderer.setYAxisMax(35);
        multiRenderer.setYAxisMin(0);
        multiRenderer.setXAxisMax(max);
        multiRenderer.setXAxisMin(0);
        multiRenderer.setShowLabels(true);
        multiRenderer.setXAxisMax(max + 1);
        multiRenderer.setLabelsTextSize(30);

        // add single renderer to multiple
        multiRenderer.addSeriesRenderer(renderer);
        multiRenderer.setShowGrid(true);
        multiRenderer.setInScroll(true);


    }

    public GraphicalView getView(Context context) {
        view = ChartFactory.getLineChartView(context, multiData, multiRenderer);
        return view;
    }

    public void addNewPoints(Point p) {
        data.add(p.x, p.y);
    }

}


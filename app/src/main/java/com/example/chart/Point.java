package com.example.chart;

public class Point {

    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Point Pointset(int x,int y) {
        return new Point(x, y);
    }

}
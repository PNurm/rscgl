package com.rscgl.model;

public class Point2D {
    int x;
    int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point2D() { }

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x: " + x + ", y: " + y + "";
    }

    public Point2D set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
}

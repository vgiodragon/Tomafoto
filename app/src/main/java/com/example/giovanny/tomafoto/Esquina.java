package com.example.giovanny.tomafoto;

import org.opencv.core.Point;

/**
 * Created by giovanny on 08/07/16.
 */

public class Esquina {
    public int x, y;
    public Point p1;
    public int w,h;

    public Esquina(Point p1, int w, int h, int nveces) {
        this.p1 = p1;
        this.w = w;
        this.h = h;
        this.nveces = nveces;
    }

    public Esquina(Point p1, int nveces) {
        this.p1 = p1;
        this.nveces = nveces;
    }

    private int nveces;

    public Esquina(int x, int y,int nveces) {
        this.x = x;
        this.y = y;
        this.nveces=nveces;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
        nveces++;
    }
    public void setP1(Point p1, int w, int h) {
        this.p1 = p1;
        this.w=w;
        this.h=h;
        nveces++;
    }

    public int getNveces(){
        return nveces;
    }
    public float distancia(Point p2){
        return (float) Math.sqrt( (p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y) );
    }

    public float distancia(int xn,int yn){
        return (float) Math.sqrt( (xn-x)*2 + (yn-y)*2 );
    }

}

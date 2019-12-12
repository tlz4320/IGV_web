package com.Treeh.Tobin.Bed;

public class Bin {
    public int getBegin() {
        return begin;
    }

    public int getWidth() {
        return width;
    }

    int begin;
    int width;
    public Bin(){
        begin = width = 0;
    }
    public Bin(int b){
        begin = b;
        width = 0;
    }
    public Bin(int b, int w){
        begin = b;
        width = w;
    }
    @Override
    public String toString(){
        return "[" + begin + "," + width + "]";
    }
}

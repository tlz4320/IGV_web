package com.Treeh.Tobin.Bed;

import java.util.HashSet;

public class DecodeBed {
    public static byte[] decodeBed(byte[] data){
        if(data == null)
            return null;
        StringBuilder builder = new StringBuilder();
        StringBuilder tempBuilder = new StringBuilder();
        boolean isBed = false;
        for (byte datum : data) {
            tempBuilder.append((char) datum);
            if (datum == '\t')
                isBed = true;
            if (datum == '\n') {
                if(isBed)
                    builder.append(tempBuilder);
                tempBuilder = new StringBuilder();
                isBed = false;

            }
        }
        return builder.toString().getBytes();
    }
    public static HashSet<String> getBedReads(byte[] data, String bedName){
        StringBuilder tempBuilder = new StringBuilder();
        boolean isBed = false;
        boolean findBed = false;
        HashSet<String> res = new HashSet<>();
        for (byte datum : data) {
            if (datum == '\n') {
                if(findBed)
                    res.add(tempBuilder.toString());
                if(!findBed && isBed){
                    String[] splited = tempBuilder.toString().split("[\t]");
                    findBed = splited[3].equals(bedName);
                }
                tempBuilder = new StringBuilder();
                isBed = false;
                continue;
            }
            tempBuilder.append((char) datum);
            if (datum == '\t') {
                isBed = true;
                if(findBed)
                    break;
            }
        }
        return res;
    }
}

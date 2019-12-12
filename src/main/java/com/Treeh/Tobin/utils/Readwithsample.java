package com.Treeh.Tobin.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Readwithsample {

    public HashMap<String, HashSet<String>> sample2read;
    public Readwithsample(){
        sample2read = new HashMap<>();

    }
    public HashSet<String> getReads(String samplename){
        return sample2read.get(samplename);
    }
    public HashMap<String, HashSet<String>> getSample2read(){
        return sample2read;
    }
    public int size(String samplename){
        if(!sample2read.containsKey(samplename))
            return 0;
        return sample2read.get(samplename).size();
    }
    public Set<String> getSamplenames(){
        return sample2read.keySet();
    }
    //        public Readwithsample(String readname, String samplename){
//            read2sample = new HashMap<>();
//            sample2read = new HashMap<>();
//            read2sample.put(readname, samplename);
//            HashSet<String> newset = new HashSet<>();
//            newset.add(readname);
//            sample2read.put(samplename, newset);
//        }
    public void add(String readname, String samplename){
        if (sample2read.containsKey(samplename))
            sample2read.get(samplename).add(readname);

        else {
            HashSet<String> newset = new HashSet<>();
            newset.add(readname);
            sample2read.put(samplename, newset);
        }
    }
    public Readwithsample(String s){
        String temp[] = s.split("[\t]");
    }
}

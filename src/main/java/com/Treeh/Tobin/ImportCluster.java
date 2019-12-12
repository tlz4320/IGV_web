package com.Treeh.Tobin;


import com.Treeh.Tobin.utils.Readwithsample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ImportCluster {
    HashMap<String, String> read2sample;
    HashSet<String> samplenames;
    String clusterfile;
    HashMap<String, Readwithsample> Data;
    public ImportCluster(String filename){
        clusterfile = filename;
        Data = new HashMap<>();
        read2sample = new HashMap<>();
        samplenames = new HashSet<>();
    }
    public HashMap<String, Readwithsample> getData(){
        return Data;
    }
    public void loadData(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(clusterfile));
            String line;
            while((line = reader.readLine()) != null){
                if(line.length() < 2)
                    continue;
                String temp[] = line.split("[\t]");
                if(temp.length != 2)
                    continue;
                int num = Integer.parseInt(temp[1]);
                Readwithsample readwithsample = null;
                if(Data.containsKey(temp[0]))
                    readwithsample = Data.get(temp[0]);
                else {
                    readwithsample = new Readwithsample();
                    Data.put(temp[0], readwithsample);
                }

                for(int index = 0; index < num ;index++){
                    line = reader.readLine();
                    if(line == null)
                        break;
                    temp = line.split("[\t]");
                    readwithsample.add(temp[0], temp[1]);
                    read2sample.put(temp[0], temp[1]);
                    samplenames.add(temp[1]);
                }
            }
        }catch (Exception E){
            throw new RuntimeException(E);
        }
    }

}

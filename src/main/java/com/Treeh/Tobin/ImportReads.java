package com.Treeh.Tobin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class ImportReads {
    private HashMap<String, String> read2sample;
    public ImportReads(){
        read2sample = new HashMap<>();
    }
    public void loadData(String path){
        try {
            File file = new File(path);
            LinkedList<File> samples = new LinkedList<>();
            if(file.isFile())
                samples.add(file);
            else{
                File[] files = file.listFiles();
                if(files != null) {
                    samples.addAll(Arrays.asList(files));
                }
            }
            for(File f : samples) {
                String samplename = f.getName();
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;
                while((line = reader.readLine()) != null){
                    if(line.length() < 2)
                        continue;
                    read2sample.put(line, samplename);
                }
                reader.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public String query(String read){
        return read2sample.getOrDefault(read, null);
    }
}

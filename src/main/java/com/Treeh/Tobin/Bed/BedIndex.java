package com.Treeh.Tobin.Bed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

public class BedIndex {
    HashMap<String, Bins> index;
    public BedIndex(){
        index = new HashMap<>();
    }
    public HashSet<Bin> getRegin(String chr, int b, int e){
        HashSet<Bin> res = new HashSet<>();
        if(!index.containsKey(chr))
            return res;
        Bins bedbins = index.get(chr);
        int beginbinindex = b / bedbins.step < 14 ? b / bedbins.step:14;
        int endbinindex = e/bedbins.step < 14 ? e/bedbins.step : 13;
        int shift = 0;
        for(int i = 14; i > 0 && beginbinindex <= endbinindex; i--, endbinindex--){
            Bin libin = new Bin(bedbins.bins[shift + beginbinindex].begin);
            for(int index = beginbinindex; index <= endbinindex; index++)
                libin.width += bedbins.bins[index + shift].width;
            shift += i;
            if(libin.width != 0)
                res.add(libin);
        }
        return res;
    }
    public void readIndex(String path){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                if(line.length() < 2)
                    continue;
                String[] splited = line.split("[\t]");
                Bins bedbins = new Bins();
                index.put(splited[0], bedbins);
                bedbins.maxlength = Integer.parseInt(splited[1]);
                bedbins.step = bedbins.maxlength / 14 + 1;
                int binlistsize = Integer.parseInt(splited[2]);
                for(int i = 0; i < binlistsize; i++){
                    line = reader.readLine();
                    if(line == null || line.length() < 2)
                        break;
                    splited = line.split("[\t]");
                    bedbins.bins[i] = new Bin(Integer.parseInt(splited[0]), Integer.parseInt(splited[1]));
                }
                for(;binlistsize <= 105; binlistsize++)bedbins.bins[binlistsize] = new Bin();
            }


        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

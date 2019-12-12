package com.Treeh.Tobin;

import com.Treeh.Tobin.Bam.Bamfactory;
import htsjdk.samtools.SAMRecord;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Util {
    static byte[]makefinaldata(HashMap<String, HashSet<SAMRecord>> selectreads, int total){
        Bamfactory bamfactory = new Bamfactory();
        byte[][] res= new byte[total][];
        int index = 0;
        for(Map.Entry<String, HashSet<SAMRecord>> e : selectreads.entrySet()){
            for(SAMRecord r : e.getValue()) {
                res[index] = bamfactory.sam2bam(r);
                index++;
            }
        }
        return byteMergerAll(res);
    }
    static public int writeString(byte[] res, int offset, String s){
        for(byte b : s.getBytes())
            res[offset++] = b;
        res[offset++] = 0;
        return offset;
    }
    static public int writeInt(byte[] res, int offset, int data){
        res[offset] = (byte)data;
        res[offset + 1] = (byte)(data >> 8);
        res[offset + 2] = (byte)(data >> 16);
        res[offset + 3] = (byte)(data >> 24);
        return offset + 4;
    }
    static byte[]makefinaldata(HashSet<SAMRecord> list, String samplename, int totalnum){
        Bamfactory bamfactory = new Bamfactory();
        byte[][] res= new byte[list.size() + 1][];
        res[0] = new byte[samplename.length() + 9];
        int index = 0;
        index = writeInt(res[0], index, samplename.length());
        index = writeString(res[0], index, samplename);
        writeInt(res[0], index, totalnum);
        index = 1;
        for(SAMRecord s : list) {
            res[index] = bamfactory.sam2bam(s);
            index++;
        }
        return byteMergerAll(res);
    }
    public static byte[] byteMergerAll(byte[][] values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }
    public static byte[] fileToByte(String name, long begin, long end) throws Exception{
        long tail = end;
        if(end == -1){
            File file = new File(name);
            tail = file.length() - 1;
        }
        byte[] bytes = new byte[(int)(tail - begin + 1)];
        FileInputStream f = new FileInputStream(name);
        long skipped = f.skip(begin);
        if(skipped != begin)
            return null;
        f.read(bytes, 0, (int)(tail - begin + 1));
        return bytes;
    }
}

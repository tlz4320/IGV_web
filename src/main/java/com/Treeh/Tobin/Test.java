package com.Treeh.Tobin;

import com.Treeh.Tobin.Bam.Bamfactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;

@Controller
public class Test {
    @RequestMapping(value = "/test/{filename}")
    public ResponseEntity<byte[]> test(HttpServletRequest request, @PathVariable("filename") String filename) throws Exception {
        SamReader reader = SamReaderFactory.makeDefault().open(new File("D:\\program\\code\\4\\igv.js\\examples\\1.bam"));
        SAMRecordIterator it = reader.queryContained("chrIS",4347598, 4389262);
        Bamfactory bamfactory = new Bamfactory();
        ArrayList<byte[]> list = new ArrayList<>();
        while(it.hasNext()){
            SAMRecord r = it.next();
            list.add(bamfactory.sam2bam(r));
        }
        int length_byte = 0;
        for (byte[] i : list) {
            length_byte += i.length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (byte[] i : list) {
            System.arraycopy(i, 0, all_byte, countLength, i.length);
            countLength += i.length;
        }
        HttpHeaders headers = new HttpHeaders();
        return  new ResponseEntity<>(all_byte, headers, HttpStatus.PARTIAL_CONTENT);
//        chrIS:4,347,598-4,389,262
    }
}

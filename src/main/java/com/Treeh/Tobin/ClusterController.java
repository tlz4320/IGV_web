package com.Treeh.Tobin;


import com.Treeh.Tobin.Bed.Bin;
import com.Treeh.Tobin.Bed.DecodeBed;
import com.Treeh.Tobin.utils.MConfig;
import com.Treeh.Tobin.utils.Readwithsample;
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
import java.util.*;

import static com.Treeh.Tobin.Bed.DecodeBed.decodeBed;
import static com.Treeh.Tobin.TobinApplication.index;
import static com.Treeh.Tobin.TobinApplication.samplename;
import static com.Treeh.Tobin.Util.fileToByte;
import static com.Treeh.Tobin.Util.makefinaldata;

@Controller
public class ClusterController {
    @RequestMapping(value = "/cluster/{filename}")
    public ResponseEntity<byte[]> getCluterReads(HttpServletRequest request, @PathVariable("filename") String filename) throws Exception{
        String clustername = request.getHeader("clustername");
        String chr = request.getHeader("chr");
        String start = request.getHeader("start");
        String end = request.getHeader("end");
        String sample = request.getHeader("sample");
        HttpHeaders headers = new HttpHeaders();
        if(clustername == null || chr == null || clustername.length() < 2 || chr.length() == 0) {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            boolean flag = true;
            for(String s : samplename){
                if(flag)
                    flag = !flag;
                else
                    builder.append(',');
                builder.append('\"');
                builder.append(s);
                builder.append('\"');
            }
            builder.append(']');
            byte[] res = builder.toString().getBytes();
            return new ResponseEntity<>(res, headers, HttpStatus.OK);
        }
        if(sample.equals("default"))
            sample = samplename.get(0);
        if(start == null || end == null || sample == null)
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        byte[] res = getreads(clustername, sample, chr, Integer.parseInt(start), Integer.parseInt(end));
        if(res == null)
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        else
            return new ResponseEntity<>(res, headers, HttpStatus.PARTIAL_CONTENT);
    }

    static public byte[] getreads(String clustername, String sample, String chr, int start, int end) throws Exception {
        if(!samplename.contains(sample))
            sample = samplename.get(0);
        SamReader reader = SamReaderFactory.makeDefault().open(new File(MConfig.prefix + sample));
        chr = chr.substring(3);
        SAMRecordIterator it = reader.queryContained(chr, start, end);
        HashSet<Bin> set = index.getRegin(chr, start, end);
        HashSet<String> samplereads = new HashSet<>();
        for(Bin bin : set) {
            samplereads.addAll(
                    DecodeBed.getBedReads(fileToByte(MConfig.prefix + "out2.txt", bin.getBegin(), bin.getBegin() + bin.getWidth() - 1),
                            clustername));
        }
        String readname;
        HashSet<SAMRecord> list = new HashSet<>();
        int totalread = 0;
        while (it.hasNext()) {
            SAMRecord r = it.next();
            readname = r.getReadName();
            if(samplereads.contains(readname)) {
                list.add(r);
                totalread++;
            }
        }
        it.close();
        int maxrows = 500;
        Random random = new Random();
        HashSet<SAMRecord> list2;
        if (totalread > maxrows) {
            double threshold = 1.2 * maxrows / (double) list.size();
            list2 = new HashSet<>();
            for (SAMRecord r : list) {
                if (random.nextDouble() < threshold)
                    list2.add(r);
                if (list2.size() >= maxrows)
                    break;
            }
            Iterator<SAMRecord> i = list.iterator();
            while (list2.size() <= maxrows && i.hasNext())
                list2.add(i.next());
        }
        else
            list2 = list;
        return makefinaldata(list2, sample, totalread);
    }
}
